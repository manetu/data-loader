;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.commands
  (:require [clojure.pprint :refer [cl-format]]
            [manetu.data-loader.commands.create-vaults :as create-vaults]
            [manetu.data-loader.commands.delete-vaults :as delete-vaults]
            [manetu.data-loader.commands.attributes.update.load :as load-attributes]
            [manetu.data-loader.commands.attributes.update.onboard :as onboard]
            [manetu.data-loader.commands.attributes.delete :as delete-attributes]
            [manetu.data-loader.commands.attributes.query :as query-attributes]))

(def command-map
  {create-vaults/command       create-vaults/spec
   delete-vaults/command       delete-vaults/spec
   load-attributes/command     load-attributes/spec
   onboard/command             onboard/spec
   delete-attributes/command   delete-attributes/spec
   query-attributes/command    query-attributes/spec})

(defn get-handler
  [subcommand]
  (some-> (get command-map subcommand) :fn))

(defn render-description []
  (mapv (fn [[command {:keys [description]}]]
          (cl-format nil (str " - " command ": ~24T" description)))
        command-map))
