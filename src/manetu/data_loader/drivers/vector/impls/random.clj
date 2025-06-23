;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.drivers.vector.impls.random
  (:require [taoensso.timbre :as log]
            [manetu.data-loader.drivers.vector.api :as api]))

(defrecord RandomDriver [dim]
  api/VectorDriver
  (create-embedding [this record]
    (repeatedly dim #(rand 1.0))))

(defn create
  [{:keys [random-dim] :as options}]
  (log/debug "Initialize random-vector generator with dimension:" random-dim)
  (RandomDriver. random-dim))
