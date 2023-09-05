;; Copyright © 2020 Manetu, Inc.  All rights reserved

(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [manetu.data-loader.main :as main]))

(defn run
  [params]
  (apply main/-app (clojure.string/split params #" ")))
