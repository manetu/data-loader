;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.commands.attributes.query
  (:require [manetu.data-loader.core :as core]
            [manetu.data-loader.drivers.transport.api :as transport.api]))

(def command "query-attributes")
(def description "Queries attributes based on each record within the input file")

(def options-spec
  [["-h" "--help"]])

(def spec {:description description
           :fn (partial core/exec {:command        command
                                   :description    description
                                   :options-spec   options-spec
                                   :factory-fn     (fn [_ transport-driver]
                                                     (partial transport.api/query-attributes transport-driver))})})
