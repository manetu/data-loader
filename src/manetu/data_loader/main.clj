;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.main
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [taoensso.timbre :as log]
            [manetu.data-loader.core :as core]
            [manetu.data-loader.commands :as commands]
            [manetu.data-loader.driver.core :as driver.core])
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

(def drivers (into #{} (keys driver.core/driver-map)))
(defn print-drivers []
  (str "[" (string/join ", " (map name drivers)) "]"))
(def driver-description
  (str "Select the driver from: " (print-drivers)))

(def modes (into #{} (keys commands/command-map)))
(defn print-modes []
  (str "[" (string/join ", " (map name modes)) "]"))
(def mode-description
  (str "Select the mode from: " (print-modes)))

(def options
  [["-h" "--help"]
   ["-v" "--version" "Print the version and exit"]
   ["-u" "--url URL" "The connection URL"]
   ["-i" "--insecure" "Disable TLS checks (dev only)"
    :default false]
   [nil "--[no-]progress" "Enable/disable progress output (default: enabled)"
    :default true]
   [nil "--provider SID" "The service-provider"
    :default "manetu.com"]
   [nil "--userid USERID" "The id of the user to login with"]
   ["-p" "--password PASSWORD" "The password of the user"]
   ["-t" "--token TOKEN" "A personal access token"]
   ["-l" "--log-level LEVEL" loglevel-description
    :default :info
    :parse-fn keyword
    :validate [log-levels (str "Must be one of " (print-loglevels))]]
   [nil "--fatal-errors" "Any sub-operation failure is considered to be an application level failure"
    :default false]
   [nil "--verbose-errors" "Any sub-operation failure is logged as ERROR instead of TRACE"
    :default false]
   [nil "--type TYPE" "The type of data source this CLI represents"
    :default "data-loader"]
   [nil "--id ID" "The id of the data-source this CLI represents"
    :default "535CC6FC-EAF7-4CF3-BA97-24B2406674A7"]
   [nil "--class CLASS" "The schemaClass of the data-source this CLI represents"
    :default "global"]
   ["-c" "--concurrency NUM" "The number of parallel requests to issue"
    :default 16
    :parse-fn #(Integer/parseInt %)
    :validate [pos? "Must be a positive integer"]]
   ["-m" "--mode MODE" mode-description
    :default :load-attributes
    :parse-fn keyword
    :validate [modes (str "Must be one of " (print-modes))]]
   ["-d" "--driver DRIVER" driver-description
    :default :graphql
    :parse-fn keyword
    :validate [drivers (str "Must be one of " (print-drivers))]]])

(defn exit [status msg & args]
  (do
    (apply println msg args)
    status))

(defn version [] (str "manetu-data-loader version: v" (System/getProperty "data-loader.version")))

(defn prep-usage [msg] (->> msg flatten (string/join \newline)))

(defn usage [options-summary]
  (prep-usage [(version)
               ""
               "Usage: manetu-data-loader [options] <file.json>"
               ""
               "Options:"
               options-summary]))

(defn has-auth?
  [{:keys [userid token]}]
  (or (some? userid) (some? token)))

(defn -app
  [& args]
  (let [{{:keys [help log-level] :as options} :options :keys [arguments errors summary]} (parse-opts args options)]
    (cond

      help
      (exit 0 (usage summary))

      (not= errors nil)
      (exit -1 "Error: " (string/join errors))

      (:version options)
      (exit 0 (version))

      (not (has-auth? options))
      (exit -1 "Must specify --userid or --token")

      (zero? (count arguments))
      (exit -1 (usage summary))

      :else
      (do
        (set-logging log-level)
        (core/exec options (first arguments))))))

(defn -main
  [& args]
  (System/exit (apply -app args)))
