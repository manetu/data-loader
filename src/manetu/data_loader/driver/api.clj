;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.driver.api)

(defprotocol Driver
  (create-vault [this record])
  (delete-vault [this record])
  (load-attributes [this record])
  (delete-attributes [this record])
  (query-attributes [this record]))
