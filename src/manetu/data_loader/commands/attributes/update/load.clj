;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.commands.attributes.update.load
  (:require [manetu.data-loader.core :as core]
            [manetu.data-loader.commands.attributes.update.common :as common]))

(def command "load-attributes")
(def description "Loads attributes to vaults previously created with 'create-vaults' subcommand")

(def spec {:description description
           :fn (partial core/exec {:command        command
                                   :description    description
                                   :options-spec   common/options-spec
                                   :factory-fn     (fn [options transport-driver]
                                                     (let [ctx (common/create-ctx options transport-driver)]
                                                       (partial common/load-attributes ctx)))})})
