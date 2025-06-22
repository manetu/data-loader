;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.main
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [taoensso.timbre :as log]
            [slingshot.slingshot :refer [throw+ try+]]
            [manetu.data-loader.commands :as commands]
            [manetu.data-loader.utils :refer [prep-usage tool-name usage-preamble
                                              help-exit error-exit exit version] :as utils])
  (:gen-class))

(defn set-logging
  [level]
  (log/set-config!
   {:level level
    :ns-whitelist  ["manetu.*"]
    :appenders
    {:custom
     {:enabled? true
      :async false
      :fn (fn [{:keys [timestamp_ msg_ level] :as data}]
            (binding [*out* *err*]
              (println (force timestamp_) (string/upper-case (name level)) (force msg_))))}}}))

(def log-levels #{:trace :debug :info :error})
(defn print-loglevels []
  (str "[" (string/join ", " (map name log-levels)) "]"))
(def loglevel-description
  (str "Select the logging verbosity level from: " (print-loglevels)))

;; (def drivers (into #{} (keys driver.core/driver-map)))
;; (defn print-drivers []
;;   (str "[" (string/join ", " (map name drivers)) "]"))
;; (def driver-description
;;   (str "Select the driver from: " (print-drivers)))

(def options-spec
  [["-h" "--help"]
   ["-v" "--version" "Print version info and exit"]
   ["-u" "--url URL" "The URL to a Manetu instance"
    :default "https://localhost"]
   ["-i" "--insecure" "Disable TLS checks"
    :default false]
   [nil "--[no-]progress" "Enable/disable progress output"
    :default true]
   ["-t" "--token TOKEN" "A Manetu personal access token"]
   ["-l" "--log-level LEVEL" loglevel-description
    :default :info
    :parse-fn keyword
    :validate [log-levels (str "Must be one of " (print-loglevels))]]
   [nil "--fatal-errors" "Any sub-operation failure is considered to be an application level failure"
    :default false]
   [nil "--verbose-errors" "Any sub-operation failure is logged as ERROR instead of TRACE"
    :default false]
   ["-c" "--concurrency NUM" "The number of parallel requests to issue"
    :default 16
    :parse-fn #(Integer/parseInt %)
    :validate [pos? "Must be a positive integer"]]
   ["-f" "--input-file FILE" "The input file of records in CSV or JSON format"
    :default "input.csv"]
   ;; ["-t" "--transport DRIVER" driver-description   FIXME - we currently only have :graphql, so we should remove this
   ;;  :default :graphql
   ;;  :parse-fn keyword
   ;;  :validate [drivers (str "Must be one of " (print-drivers))]]
   ])

(defn usage [options-summary]
  (prep-usage (-> [(str usage-preamble " subcommand [subcommand-options]")
                   ""
                   "Subcommands:"]
                  (concat
                   (commands/render-description))
                  (concat
                   [""
                    "Global Options:"
                    options-summary
                    ""
                    (str "Use '" tool-name " <subcommand> -h' for subcommand specific help")]))))

(defn -app
  [& args]
  (let [{{:keys [help log-level url token] :as global-options} :options
         global-summary :summary
         :keys [arguments errors]}
        (parse-opts args options-spec :in-order true)
        subcommand (first arguments)
        usage-summary (usage global-summary)]
    (try+
     (cond

       help
       (help-exit usage-summary)

       (not= errors nil)
       (error-exit (string/join errors) usage-summary)

       (:version global-options)
       (exit 0 (version))

       (string/blank? subcommand)
       (error-exit "subcommand required" usage-summary)

       :else
       (do
         (set-logging log-level)
         (if-let [exec-fn (commands/get-handler subcommand)]
           (exec-fn global-summary (assoc global-options :transport :graphql) arguments)
           (error-exit (str "unknown subcommand: \"" subcommand "\"") (usage global-summary)))))
     (catch [:type ::utils/exit] {:keys [status msg]}
       (println msg)
       status))))

(defn -main
  [& args]
  (System/exit (apply -app args)))
