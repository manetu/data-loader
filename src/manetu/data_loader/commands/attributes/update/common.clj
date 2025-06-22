;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.commands.attributes.update.common
  (:require [clojure.string :as string]
            [cheshire.core :as json]
            [manetu.data-loader.drivers.transport.api :as transport.api]
            [manetu.data-loader.drivers.vector.api :as vector.api]
            [manetu.data-loader.drivers.vector.core :as vector.core]))

(def drivers (into #{} (keys vector.core/driver-map)))
(defn print-drivers []
  (str "[" (string/join ", " (map name drivers)) "]"))
(def driver-description
  (str "Select the driver from: " (print-drivers) "  The 'null' driver disables vector embeddings."))

(def options-spec
  [["-h" "--help"]
   ["-v" "--vector-provider DRIVER" driver-description
    :default "null"
    :validate [drivers (str "Must be one of " (print-drivers))]]
   [nil "--ollama-url" "The URL to an ollama instance.  Only valid with '--vector-provider ollama'"
    :default "http://localhost:11434"]
   [nil "--ollama-model" "The ollama model.  Only valid with '--vector-provider ollama'"
    :default "llama3.2"]
   [nil "--id ID" "The RDF id to be applied the data source"
    :default "535CC6FC-EAF7-4CF3-BA97-24B2406674A7"]
   [nil "--type TYPE" "the RDF type of the data source"
    :default "data-loader"]
   [nil "--class CLASS" "The RDF schemaClass applied to the data source"
    :default "global"]])

(defn create-ctx [options transport-driver]
  (let [vector-driver (vector.core/create options)]
    {:transport-driver transport-driver
     :vector-driver    vector-driver}))

(defn load-attributes
  [{:keys [vector-driver transport-driver] :as ctx} record]
  (let [embedding (some-> (vector.api/create-embedding vector-driver record)
                          (json/generate-string))]
    (transport.api/load-attributes transport-driver (assoc-in record [:data :embedding] embedding))))
