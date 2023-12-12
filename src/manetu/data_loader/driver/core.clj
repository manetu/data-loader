;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.driver.core
  (:require [manetu.data-loader.driver.drivers.grpc.core :as grpc]))

(def driver-map
  {:grpc grpc/create})

(defn create [{:keys [driver] :as options}]
  (if-let [create-fn (get driver-map driver)]
    (create-fn options)
    (throw (ex-info "unknown driver" {:type driver}))))
