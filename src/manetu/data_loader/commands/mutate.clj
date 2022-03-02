;; Copyright © 2020-2022 Manetu, Inc.  All rights reserved

(ns manetu.data-loader.commands.mutate
  (:require [promesa.core :as p]
            [manetu.data-loader.utils :as utils]
            [manetu.data-loader.sparql :as sparql]
            [manetu.api.account.v1.Accounts.client :as account.client]
            [manetu.api.attribute.v1.RDF.client :as attribute.client]
            [taoensso.timbre :as log]))

(defn create-vault
  [options client {:keys [label] {:keys [Email]} :data}]
  (log/trace (str "creating vault \"" label "\""))
  (account.client/CreateVault client {:header (utils/create-header)
                                      :email Email
                                      :label label}))

(defn load-attributes
  [options client {:keys [label data]}]
  (let [expr (sparql/convert options data)]
    (log/trace (str "update-attributes-> vault:" label " expr:" expr))
    (attribute.client/UpdateAttributes client {:header (utils/create-header)
                                               :vault-label label
                                               :sparql-expr expr})))

(defn onboard
  [options client record]
  (-> (create-vault options client record)
      (p/then (fn [_] (load-attributes options client record)))))

(defn delete-attributes
  [options client {:keys [label]}]
  (log/trace (str "delete-attributes-> vault:" label))
  (attribute.client/UpdateAttributes client {:header (utils/create-header)
                                             :vault-label label
                                             :sparql-expr "DELETE WHERE { ?e ?a ?v . }"}))
