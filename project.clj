(defproject manetu/data-loader "1.2.1-SNAPSHOT"
  :description "A utility to load and verify data on the Manetu Platform"
  :url "https://github.com/manetu/data-loader"
  :plugins [[lein-cljfmt "0.9.0"]
            [lein-kibit "0.1.8"]
            [lein-bikeshed "0.5.2"]
            [lein-cloverage "1.2.3"]
            [jonase/eastwood "1.3.0"]
            [lein-bin "0.3.5"]]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.cli "1.0.214"]
                 [io.pedestal/pedestal.service "0.5.10"]
                 [io.github.protojure/grpc-client "2.6.0"]
                 [io.github.protojure/google.protobuf "2.0.1"]
                 [com.google.protobuf/protobuf-java "3.21.10"]
                 [com.taoensso/timbre "6.0.2"]
                 [com.fzakaria/slf4j-timbre "0.3.21"]
                 [org.slf4j/jul-to-slf4j "2.0.5"]
                 [org.slf4j/jcl-over-slf4j "2.0.5"]
                 [org.slf4j/log4j-over-slf4j "2.0.5"]
                 [org.eclipse.jetty.http2/http2-client "11.0.12"]
                 [org.eclipse.jetty/jetty-alpn-java-client "11.0.12"]
                 [cheshire "5.11.0"]
                 [danlentz/clj-uuid "0.1.9"]
                 [org.clojure/core.match "1.0.0"]
                 [progrock "0.1.2"]
                 [doric "0.9.0"]
                 [clj-commons/fs "1.6.310"]
                 [org.clojure/data.csv "1.0.1"]
                 [kixi/stats "0.5.5"]
                 [de.ubercode.clostache/clostache "1.4.0"]
                 [buddy/buddy-sign "3.4.333"]
                 [medley "1.4.0"]
                 [slingshot "0.12.2"]]
  :main ^:skip-aot manetu.data-loader.main
  :target-path "target/%s"
  :uberjar-name "app.jar"
  :jvm-opts ["-server"]
  :resource-paths ["libs/protos-1.7.0-11.jar"]

  :bin {:name "manetu-data-loader"
        :bin-path "target"
        :bootclasspath false}

  :eastwood {:add-linters [:unused-namespaces]
             :exclude-linters [:deprecations :suspicious-expression :local-shadows-var :unused-meta-on-macro :reflection]}

  ;; nREPL by default starts in the :main namespace, we want to start in `user`
  ;; because that's where our development helper functions like (refresh) live.
  :repl-options {:init-ns user}

  :profiles {:dev {:dependencies [[clj-http "3.12.3"]
                                  [org.clojure/tools.namespace "1.3.0"]
                                  [criterium "0.4.6"]]}
             :uberjar {:aot :all}})
