(defproject manetu/data-loader "3.0.0-SNAPSHOT"
  :description "A utility to load and verify data on the Manetu Platform"
  :url "https://github.com/manetu/data-loader"
  :plugins [[lein-cljfmt "0.9.0"]
            [lein-kibit "0.1.8"]
            [lein-bikeshed "0.5.2"]
            [lein-cloverage "1.2.3"]
            [jonase/eastwood "1.3.0"]
            [lein-bin "0.3.5"]]
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [org.clojure/core.async "1.6.681"]
                 [org.clojure/tools.cli "1.1.230"]
                 [com.taoensso/timbre "6.5.0"]
                 [com.fzakaria/slf4j-timbre "0.4.1"]
                 [org.slf4j/jul-to-slf4j "2.0.16"]
                 [org.slf4j/jcl-over-slf4j "2.0.16"]
                 [org.slf4j/log4j-over-slf4j "2.0.16"]
                 [org.eclipse.jetty.http2/http2-client "11.0.24"]
                 [org.eclipse.jetty/jetty-alpn-java-client "12.0.14"]
                 [ring/ring-codec "1.2.0"]
                 [funcool/promesa "11.0.678"]
                 [cheshire "5.13.0"]
                 [progrock "0.1.2"]
                 [doric "0.9.0"]
                 [clj-commons/fs "1.6.311"]
                 [org.clojure/data.csv "1.1.0"]
                 [kixi/stats "0.5.7"]
                 [luhhujbb/clostache "1.5.0"]
                 [buddy/buddy-sign "3.6.1-359"]
                 [medley "1.4.0"]
                 [slingshot "0.12.2"]
                 [district0x/graphql-query "1.0.6"]
                 [http-kit/http-kit "2.7.0"]
                 [environ "1.2.0"]
                 [dev.langchain4j/langchain4j "1.0.1"]
                 [dev.langchain4j/langchain4j-ollama "1.0.1-beta6"]
                 [clj-commons/clj-yaml "1.0.29"]]
  :main ^:skip-aot manetu.data-loader.main
  :target-path "target/%s"
  :uberjar-name "app.jar"
  :jvm-opts ["-server"]

  :bin {:name "manetu-data-loader"
        :bootclasspath false}

  :eastwood {:add-linters [:unused-namespaces]
             :exclude-linters [:deprecations :suspicious-expression :local-shadows-var :unused-meta-on-macro :reflection]}

  ;; nREPL by default starts in the :main namespace, we want to start in `user`
  ;; because that's where our development helper functions like (refresh) live.
  :repl-options {:init-ns user}

  :profiles {:dev {:dependencies [[clj-http "3.13.0"]
                                  [org.clojure/tools.namespace "1.5.0"]]}
             :uberjar {:aot :all}})
