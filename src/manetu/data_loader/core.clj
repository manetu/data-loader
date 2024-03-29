;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.core
  (:require [promesa.core :as p]
            [taoensso.timbre :as log]
            [clojure.core.async :refer [>!! <! go go-loop] :as async]
            [progrock.core :as pr]
            [kixi.stats.core :as kixi]
            [doric.core :refer [table]]
            [manetu.data-loader.commands :as commands]
            [manetu.data-loader.loader :as loader]
            [manetu.data-loader.time :as t]
            [manetu.data-loader.driver.core :as driver.core]))

(defn promise-put!
  [port val]
  (p/create
   (fn [resolve reject]
     (async/put! port val resolve))))

(defn execute-command
  [{:keys [verbose-errors]} f {{:keys [Email]} :data :as record} ch]
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
             (promise-put! ch (assoc result
                                     :email Email
                                     :duration d)))))
        (p/then
         (fn [_]
           (async/close! ch))))))

(defn execute-commands
  [{:keys [concurrency] :as options} f output-ch input-ch]
  (p/create
   (fn [resolve reject]
     (go
       (log/trace "launching" concurrency "requests")
       (<! (async/pipeline-async concurrency
                                 output-ch
                                 (partial execute-command options f)
                                 input-ch))
       (resolve true)))))

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

(defn compute-summary-stats
  [options n mux]
  (transduce-promise options n mux (map :duration) kixi/summary))

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
  (println (table [:successes :failures :min :median :max :total-duration] [stats]))
  (if (and fatal-errors (pos? failures))
    -1
    0))

(defn exec
  [{:keys [mode concurrency] :as options} path]
  (let [{:keys [n] :as records} (loader/load-records path)
        output-ch (async/chan (* 4 concurrency))]
    (log/debug "processing" n "records with options:" options)
    @(-> (driver.core/create options)
         (p/then
          (fn [driver]
            (let [mux (async/mult output-ch)
                  f (commands/get-handler mode driver)]
              (p/all [(t/now)
                      (execute-commands options f output-ch (:ch records))
                      (show-progress options n mux)
                      (compute-stats options n mux)]))))
         (p/then
          (fn [[start _ _ stats]]
            (let [end (t/now)]
              (assoc stats :total-duration (t/duration end start)))))
         (p/then (partial render options))
         (p/catch
          (fn [e]
            (log/error "Exception detected:" (ex-message e))
            -1)))))
