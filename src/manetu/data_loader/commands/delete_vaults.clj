;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.commands.delete-vaults
  (:require [manetu.data-loader.core :as core]
            [manetu.data-loader.drivers.transport.api :as transport.api]))

(def command "delete-vaults")
(def description "Delete vaults and all data within for each record")

(def options-spec
  [["-h" "--help"]])

(def spec {:description description
           :fn (partial core/exec {:command        command
                                   :description    description
                                   :options-spec   options-spec
                                   :factory-fn     (fn [_ transport-driver]
                                                     (partial transport.api/delete-vault transport-driver))})})
