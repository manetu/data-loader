;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.commands.attributes.update.onboard
  (:require [promesa.core :as p]
            [manetu.data-loader.core :as core]
            [manetu.data-loader.drivers.transport.api :as transport.api]
            [manetu.data-loader.commands.attributes.update.common :as common]))

(def command "onboard")
(def description "Runs 'create-vault' and 'load-attributes' for each record")

(defn factory-fn [options transport-driver]
  (let [ctx (common/create-ctx options transport-driver)]
    (fn [record]
      (-> (transport.api/create-vault transport-driver record)
          (p/then (fn [_]
                    (common/load-attributes ctx record)))))))

(def spec {:description description
           :fn (partial core/exec {:command        command
                                   :description    description
                                   :options-spec   common/options-spec
                                   :factory-fn     factory-fn})})
