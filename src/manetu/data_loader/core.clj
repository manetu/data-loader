;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.core
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.core.async :refer [>!! <! <!! go go-loop] :as async]
            [medley.core :as m]
            [promesa.core :as p]
            [taoensso.timbre :as log]
            [progrock.core :as pr]
            [doric.core :refer [table]]
            [kixi.stats.core :as kixi]
            [manetu.data-loader.loader :as loader]
            [manetu.data-loader.time :as t]
            [manetu.data-loader.drivers.transport.core :as driver.core]
            [manetu.data-loader.stats :as stats]
            [manetu.data-loader.utils :refer [subcommand-usage help-exit error-exit] :as utils]))

(defn execute-command
  [{:keys [verbose-errors]} f {{:keys [Email]} :data :as record}]
  (log/trace "record:" record)
  (let [start (t/now)]
    (-> (f record)
        (p/then
         (fn [result]
           (log/trace "success for" Email)
           {:success true :result result}))
        (p/catch
         (fn [e]
           (if verbose-errors
             (log/error (str Email ": " (ex-message e) " " (ex-data e)))
             (log/trace "ERROR" (str Email ": " (ex-message e) " " (ex-data e))))
           {:success false :exception e}))
        (p/then
         (fn [result]
           (let [end (t/now)
                 d (t/duration end start)]
             (log/trace Email "processed in" d "msecs")
             (assoc result
                    :email Email
                    :duration d)))))))

(defn execute-commands
  [{:keys [concurrency] :as options} f output-ch input-ch]
  (-> (p/all
       (map
        (fn [_]
          (p/vthread
           (loop []
             (when-let [m (<!! input-ch)]
               (>!! output-ch @(execute-command options f m))
               (recur)))))
        (range concurrency)))
      (p/then (fn [_]
                (async/close! output-ch)
                true))))

(defn show-progress
  [{:keys [progress concurrency] :as options} n mux]
  (when progress
    (let [ch (async/chan (* 4 concurrency))]
      (async/tap mux ch)
      (p/create
       (fn [resolve reject]
         (go-loop [bar (pr/progress-bar n)]
           (if (= (:progress bar) (:total bar))
             (do (pr/print (pr/done bar))
                 (resolve true))
             (do (<! ch)
                 (pr/print bar)
                 (recur (pr/tick bar))))))))))

(defn transduce-promise
  [{:keys [concurrency] :as options} n mux xform f]
  (p/create
   (fn [resolve reject]
     (go
       (let [ch (async/chan (* 4 concurrency))]
         (async/tap mux ch)
         (let [result (<! (async/transduce xform f (f) ch))]
           (resolve result)))))))

(defn round2
  "Round a double to the given precision (number of significant digits)"
  [precision ^double d]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* d factor)) factor)))

(defn compute-summary-stats
  [options n mux]
  (-> (transduce-promise options n mux (map :duration) stats/summary)
      (p/then (fn [{:keys [dist] :as summary}]
                (-> summary
                    (dissoc :dist)
                    (merge dist)
                    (as-> $ (m/map-vals #(round2 3 (or % 0)) $)))))))

(defn successful?
  [{:keys [success]}]
  (true? success))

(defn failed?
  [{:keys [success]}]
  (false? success))

(defn count-msgs
  [options n mux pred]
  (transduce-promise options n mux (filter pred) kixi/count))

(defn compute-stats
  [options n mux]
  (-> (p/all [(compute-summary-stats options n mux)
              (count-msgs options n mux successful?)
              (count-msgs options n mux failed?)])
      (p/then (fn [[summary s f]] (assoc summary :successes s :failures f)))))

(defn render
  [{:keys [fatal-errors] :as options} {:keys [failures] :as stats}]
  (println (table [:successes :failures :min :mean :stddev :p50 :p90 :p95 :p99 :max :total-duration :rate] [stats]))
  (if (and fatal-errors (pos? failures))
    -1
    0))

(defn- exec!
  [usage-summary {:keys [input-file concurrency] :as options} factory-fn]
  {:pre [(utils/verify-global-options usage-summary options)]}
  (let [{:keys [n] :as records} (loader/load-records input-file)
        output-ch (async/chan (* 4 concurrency))]
    (log/debug "processing" n "records with options:" options)
    @(-> (driver.core/create options)
         (p/then
          (fn [transport-driver]
            (let [mux (async/mult output-ch)
                  f (factory-fn options transport-driver)]
              (p/all [(t/now)
                      (execute-commands options f output-ch (:ch records))
                      (show-progress options n mux)
                      (compute-stats options n mux)]))))
         (p/then
          (fn [[start _ _ {:keys [successes] :as stats}]]
            (let [end (t/now)
                  d (t/duration end start)]
              (assoc stats :total-duration (round2 3 d) :rate (round2 2 (* (/ successes d) 1000))))))
         (p/then (partial render options))
         (p/catch
          (fn [e]
            (log/error "Exception detected:" (ex-message e))
            -1)))))

(defn exec
  [{:keys [command description options-spec validation-fn factory-fn] :or {validation-fn (constantly true)}}
   global-summary global-options args]
  (let [{{:keys [help] :as local-options} :options
         :keys [errors summary]} (parse-opts args options-spec)
        summary (subcommand-usage command description global-summary summary)]
    (cond

      help
      (help-exit summary)

      (not= errors nil)
      (error-exit (string/join errors) summary)

      :else
      (let [options (merge global-options local-options)]
        (validation-fn options)
        (exec! summary options factory-fn)))))
