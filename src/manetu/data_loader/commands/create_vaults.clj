;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.commands.create-vaults
  (:require [manetu.data-loader.core :as core]
            [manetu.data-loader.drivers.transport.api :as transport.api]))

(def command "create-vaults")
(def description "Creates a empty vault for each record. See 'load-attributes' and 'onboard' for data loading")

(def options-spec
  [["-h" "--help"]])

(def spec {:description description
           :fn (partial core/exec {:command        command
                                   :description    description
                                   :options-spec   options-spec
                                   :factory-fn     (fn [_ transport-driver]
                                                     (partial transport.api/create-vault transport-driver))})})
