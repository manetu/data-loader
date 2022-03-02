;; Copyright © 2020-2022 Manetu, Inc.  All rights reserved

(ns manetu.data-loader.utils
  (:require [clj-uuid :as uuid]))

(defn create-header []
  (-> {:magic 0xACCEDE
       :version 0
       :txid (uuid/to-string (uuid/v4))}))
