;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.drivers.vector.api)

(defprotocol VectorDriver
  (create-embedding [this record]))
