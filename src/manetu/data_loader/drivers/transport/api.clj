;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.drivers.transport.api)

(defprotocol TransportDriver
  (create-vault [this record])
  (delete-vault [this record])
  (load-attributes [this record])
  (delete-attributes [this record])
  (query-attributes [this record]))
