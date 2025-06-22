;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.drivers.transport.core
  (:require
   [manetu.data-loader.drivers.transport.impls.graphql :as graphql]))

(def driver-map
  {:graphql graphql/create})

(defn create [{:keys [transport] :as options}]
  (if-let [create-fn (get driver-map transport)]
    (create-fn options)
    (throw (ex-info "unknown driver" {:type transport}))))
