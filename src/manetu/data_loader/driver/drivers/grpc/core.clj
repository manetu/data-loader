;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.driver.drivers.grpc.core
  (:require [promesa.core :as p]
            [taoensso.timbre :as log]
            [clojure.core.async :refer [go-loop <!] :as async]
            [clostache.parser :as clostache]
            [clojure.data :refer [diff]]
            [manetu.data-loader.utils :as utils]
            [manetu.data-loader.sparql :as sparql]
            [manetu.api.vault.v1.Vaults.client :as vault.client]
            [manetu.api.attribute.v1.RDF.client :as attribute.client]
            [manetu.data-loader.driver.drivers.grpc.login :as grpc.login]
            [manetu.data-loader.driver.api :as api]))

(defn- -create-vault
  [{:keys [client]} {:keys [label]}]
  (log/trace (str "creating vault \"" label "\""))
  (vault.client/CreateVault client {:header (utils/create-header)
                                    :label label
                                    :meta-data {:classification :cl-maximum
                                                :annotations {"io.manetu.datasubject.vault" "true"}}}))

(defn- -delete-vault
  [{:keys [client]} {:keys [label]}]
  (log/trace (str "delete vault \"" label "\""))
  (-> (vault.client/GetVault client {:header (utils/create-header)
                                     :label label})
      (p/then (fn [{{:keys [version]} :vault-info :as r}]
                (log/trace "vault:" r)
                (vault.client/DeleteVault client {:header (utils/create-header)
                                                  :label label
                                                  :version version})))))

(defn- -load-attributes
  [{:keys [client] :as options} {:keys [label data]}]
  (let [expr (sparql/convert options data)]
    (log/trace (str "update-attributes-> vault:" label " expr:" expr))
    (attribute.client/UpdateAttributes client {:header (utils/create-header)
                                               :vault-label label
                                               :sparql-expr expr})))

(defn- -delete-attributes
  [{:keys [client]} {:keys [label]}]
  (log/trace (str "delete-attributes-> vault:" label))
  (attribute.client/UpdateAttributes client {:header (utils/create-header)
                                             :vault-label label
                                             :sparql-expr "DELETE WHERE { ?e ?a ?v . }"}))

(defn- collect-results
  [ch _]
  (p/create
   (fn [resolve reject]
     (go-loop [acc []]
       (let [r (<! ch)]
         (if-not r
           (resolve acc)
           (recur (conj acc (:bindings r)))))))))

(defn- flatten-attributes
  [coll]
  (reduce (fn [acc {:strs [?attribute ?value]}] (assoc acc ?attribute ?value)) {} coll))

(def person-re #"^<http:\/\/www.w3.org\/ns\/person#(\w+)>$")

(defn- filter-person
  [coll]
  (filter (fn [[k v]] (some? (re-matches person-re k))) coll))

(defn- uri-to-attribute
  [attr]
  (keyword (second (re-find person-re attr))))

(defn- flatten-person
  [coll]
  (reduce (fn [acc [k v]] (assoc acc (uri-to-attribute k) v)) {} coll))

(defn- results-to-record
  [results]
  (-> (flatten-attributes results)
      (filter-person)
      (flatten-person)))

(defn- remove-unset
  [record]
  (into {} (remove (fn [[k v]] (empty? v)) record)))

(defn- validate
  [original result]
  (let [[a b] (apply diff (map remove-unset [original result]))]
    (log/trace "result:" result)
    (assert (every? nil? [a b]))))

(defn- -query-attributes
  [{:keys [id client] :as options} {{:keys [Email] :as record} :data}]
  (let [ch (async/chan 1024)
        sparql-expr (clostache/render sparql/query-template {:id id :email Email})]
    (log/trace (str "query-attributes:" sparql-expr))
    (-> (attribute.client/QueryAttributes client
                                          {:header (utils/create-header)
                                           :sparql-expr sparql-expr}
                                          ch)
        (p/catch (fn [e] (log/debug "Error:" e) (throw e)))
        (p/then (partial collect-results ch))
        (p/then results-to-record)
        (p/then (partial validate record)))))

(defrecord GrpcDriver [ctx]
  api/Driver
  (create-vault [this record]
    (-create-vault ctx record))
  (delete-vault [this record]
    (-delete-vault ctx record))
  (load-attributes [this record]
    (-load-attributes ctx record))
  (delete-attributes [this record]
    (-delete-attributes ctx record))
  (query-attributes [this record]
    (-query-attributes ctx record)))

(defn create
  [options]
  (p/let [client (grpc.login/connect options)]
    (GrpcDriver. (assoc options :client client))))
