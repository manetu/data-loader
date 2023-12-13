;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.commands
  (:require [promesa.core :as p]
            [manetu.data-loader.driver.api :as driver.api]))

(defn init-create-vault [driver]
  (partial driver.api/create-vault driver))

(defn init-delete-vault [driver]
  (partial driver.api/delete-vault driver))

(defn init-load-attributes [driver]
  (partial driver.api/load-attributes driver))

(defn init-onboard [driver]
  (fn [record]
    (-> (driver.api/create-vault driver record)
        (p/then (fn [_]
                  (driver.api/load-attributes driver record))))))

(defn init-delete-attributes [driver]
  (partial driver.api/delete-attributes driver))

(defn init-query-attributes [driver]
  (partial driver.api/query-attributes driver))

(def command-map
  {:create-vaults     init-create-vault
   :delete-vaults     init-delete-vault
   :load-attributes   init-load-attributes
   :onboard           init-onboard
   :delete-attributes init-delete-attributes
   :query-attributes  init-query-attributes})

(defn get-handler
  [mode driver]
  (if-let [init-fn (get command-map mode)]
    (init-fn driver)
    (throw (ex-info "bad mode" mode))))
