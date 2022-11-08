;; Copyright © 2020-2022 Manetu, Inc.  All rights reserved

(ns manetu.data-loader.commands.core
  (:require [manetu.data-loader.commands.mutate :as mutate]
            [manetu.data-loader.commands.query :as query]))

(def command-map
  {:create-vaults     mutate/create-vault
   :delete-vaults     mutate/delete-vault
   :load-attributes   mutate/load-attributes
   :onboard           mutate/onboard
   :delete-attributes mutate/delete-attributes
   :query-attributes  query/query-attributes})

(defn get-handler
  [mode]
  (get command-map mode))
