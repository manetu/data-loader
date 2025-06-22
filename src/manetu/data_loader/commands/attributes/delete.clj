;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.commands.attributes.delete
  (:require [manetu.data-loader.core :as core]
            [manetu.data-loader.drivers.transport.api :as transport.api]))

(def command "delete-attributes")
(def description "Deletes all attributes from vaults without deleting the vault itself")

(def options-spec
  [["-h" "--help"]])

(def spec {:description description
           :fn (partial core/exec {:command        command
                                   :description    description
                                   :options-spec   options-spec
                                   :factory-fn     (fn [_ transport-driver]
                                                     (partial transport.api/delete-attributes transport-driver))})})
