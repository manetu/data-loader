;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.utils
  (:require [clojure.string :as string]
            [slingshot.slingshot :refer [throw+ try+]]))

(def tool-name "manetu-data-loader")

(defn version [] (str tool-name " version: v" (System/getProperty "data-loader.version")))

(defn prep-usage [msg] (->> msg flatten (string/join \newline)))

(def usage-preamble (str "Usage: " tool-name " [global-options]"))

(defn subcommand-usage
  [subcommand description global-summary local-summary]
  (prep-usage [(str usage-preamble " " subcommand " [options]")
               ""
               description
               ""
               "Subcommand Options:"
               local-summary
               ""
               "Global Options:"
               global-summary]))

(defn exit [status msg & args]
  (throw+ {:type ::exit :status status :msg (apply str msg args)}))

(defn help-exit [summary]
  (exit 0 summary))

(defn error-exit [error-msg summary]
  (exit -1 (str "Error: " error-msg "\n\n") summary))

(defn global-option-error [field usage-summary]
  (error-exit (str "global option '" field "' required") usage-summary))

(defn verify-global-options [usage-summary {:keys [url token input-file] :as options}]
  (cond
    (string/blank? url)
    (global-option-error "--url" usage-summary)

    (string/blank? token)
    (global-option-error "--token" usage-summary)

    (string/blank? input-file)
    (global-option-error "--input-file" usage-summary)

    :else
    true))
