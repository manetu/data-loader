;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.drivers.vector.impls.null
  (:require [manetu.data-loader.drivers.vector.api :as api]))

(defrecord NullDriver [ctx]
  api/VectorDriver
  (create-embedding [this record]
    nil))

(defn create
  [options]
  (NullDriver. options))
