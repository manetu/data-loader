;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.drivers.vector.impls.ollama
  (:require [taoensso.timbre :as log]
            [clj-yaml.core :as yaml]
            [manetu.data-loader.drivers.vector.api :as api])
  (:import [dev.langchain4j.model.ollama OllamaEmbeddingModel]
           [dev.langchain4j.model.embedding EmbeddingModel]
           [dev.langchain4j.data.segment TextSegment]
           [dev.langchain4j.data.document Metadata]))

(defn create-embedding*
  [{:keys [^EmbeddingModel model] :as ctx} {:keys [data] :as record}]
  (log/debug "create-embedding:" data)
  (-> (yaml/generate-string data :dumper-options {:flow-style :block})
      (TextSegment. (Metadata.))
      (as-> $ (.embed model $))
      (.content)
      (.vector)
      (as-> $ (into [] $))))

(defrecord OllamaDriver [ctx]
  api/VectorDriver
  (create-embedding [this record]
    (create-embedding* ctx record)))

(defn create
  [{:keys [ollama-url ollama-model] :as options}]
  (let [model (-> (OllamaEmbeddingModel/builder)
                  (.baseUrl ollama-url)
                  (.modelName ollama-model)
                  (.build))]
    (OllamaDriver. (assoc options :model model))))
