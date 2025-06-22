;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.drivers.vector.impls.random
  (:require [manetu.data-loader.drivers.vector.api :as api]))

(defrecord RandomDriver [ctx]
  api/VectorDriver
  (create-embedding [this record]
    (repeatedly 1536 #(rand 1.0))))

(defn create
  [options]
  (RandomDriver. options))
