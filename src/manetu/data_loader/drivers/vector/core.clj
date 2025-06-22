;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.drivers.vector.core
  (:require [manetu.data-loader.drivers.vector.impls.null :as null]
            [manetu.data-loader.drivers.vector.impls.random :as random]
            [manetu.data-loader.drivers.vector.impls.ollama :as ollama]))

(def driver-map
  {"null"   null/create
   "random" random/create
   "ollama" ollama/create})

(defn create [{:keys [vector-provider] :as options}]
  (if-let [create-fn (get driver-map vector-provider)]
    (create-fn options)
    (throw (ex-info "unknown driver" {:type vector-provider}))))
