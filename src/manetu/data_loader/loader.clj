;; Copyright © 2020-2022 Manetu, Inc.  All rights reserved

(ns manetu.data-loader.loader
  (:require [buddy.core.hash :as hash]
            [buddy.core.codecs.base64 :as b64]
            [cheshire.core :as json]
            [clojure.core.async :refer [<!!]]
            [clojure.data.csv :as csv]
            [me.raynes.fs :as fs]
            [clojure.java.io :as io]
            [clojure.core.async :refer [go] :as async]))

(defn- compute-label
  [email]
  (-> (hash/sha256 email)                                   ;; This should normally be an HMAC
      (b64/encode true)
      (String. "UTF-8")))

(defn compute-labels
  [records]
  (map (fn [{:keys [Email] :as data}]
         {:label (compute-label Email) :data data})
       records))

(defn load-json
  [rdr]
  (json/parse-stream rdr true))

(defn csv->maps [data]
  (map zipmap
       (->> (first data) ;; First row is the header
            (map keyword) ;; Drop if you want string keys instead
            repeat)
       (rest data)))

(defn load-csv
  [rdr]
  (csv->maps (csv/read-csv rdr)))

(defn parse
  [path rdr]
  (let [ext (fs/extension path)]
    (case ext
      ".json" (load-json rdr)
      ".csv" (load-csv rdr))))

(defn record-seq
  [path f]
  (with-open [rdr (io/reader path)]
    (f (parse path rdr))))

(defn load-records
  [path]
  (let [n (record-seq path count)
        ch (async/chan 1024)]
    (go
      (record-seq path #(<!! (async/onto-chan!! ch (compute-labels %)))))
    {:n n :ch ch}))
