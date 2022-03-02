;; Copyright © 2020-2022 Manetu, Inc.  All rights reserved

(ns manetu.data-loader.commands.query
  (:require [manetu.data-loader.utils :as utils]
            [manetu.api.attribute.v1.RDF.client :as attribute.client]
            [taoensso.timbre :as log]
            [promesa.core :as p]
            [clojure.core.async :refer [go-loop <!] :as async]
            [clostache.parser :as clostache]
            [clojure.data :refer [diff]]))

(def template
  "
   PREFIX person: <http://www.w3.org/ns/person#>
   PREFIX manetu: <http://manetu.com/manetu/>

   SELECT ?attribute ?value
   WHERE {?root   manetu:email \"{{email}}\" ;
                  manetu:hasSource ?src .
          ?src    manetu:id \"{{id}}\" ;
                  manetu:hasPerson ?person .
          ?person ?attribute ?value .
          }")

(defn collect-results
  [ch _]
  (p/create
   (fn [resolve reject]
     (go-loop [acc []]
       (let [r (<! ch)]
         (if-not r
           (resolve acc)
           (recur (conj acc (:bindings r)))))))))

(defn flatten-attributes
  [coll]
  (reduce (fn [acc {:strs [?attribute ?value]}] (assoc acc ?attribute ?value)) {} coll))

(def person-re #"^<http:\/\/www.w3.org\/ns\/person#(\w+)>$")

(defn filter-person
  [coll]
  (filter (fn [[k v]] (some? (re-matches person-re k))) coll))

(defn uri-to-attribute
  [attr]
  (keyword (second (re-find person-re attr))))

(defn flatten-person
  [coll]
  (reduce (fn [acc [k v]] (assoc acc (uri-to-attribute k) v)) {} coll))

(defn results-to-record
  [results]
  (-> (flatten-attributes results)
      (filter-person)
      (flatten-person)))

(defn remove-unset
  [record]
  (into {} (remove (fn [[k v]] (empty? v)) record)))

(defn validate
  [original result]
  (let [[a b] (apply diff (map remove-unset [original result]))]
    (log/trace "result:" result)
    (assert (every? nil? [a b]))))

(defn query-attributes
  [{:keys [id] :as options} client {{:keys [Email] :as record} :data}]
  (let [ch (async/chan 1024)
        sparql-expr (clostache/render template {:id id :email Email})]
    (log/trace (str "query-attributes:" sparql-expr))
    (-> (attribute.client/QueryAttributes client
                                          {:header (utils/create-header)
                                           :sparql-expr sparql-expr}
                                          ch)
        (p/catch (fn [e] (log/debug "Error:" e) (throw e)))
        (p/then (partial collect-results ch))
        (p/then results-to-record)
        (p/then (partial validate record)))))
