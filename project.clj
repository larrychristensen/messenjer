(defproject messenjer "0.1.0-SNAPSHOT"
  :description "The next big thing in messenjing!"
  :url "http://lambdastew.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main messenjer.server
  
  :min-lein-version "2.7.1"
  
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async  "0.4.474"]
                 [garden "1.3.2"]
                 [reagent "0.7.0"]
                 [re-frame "0.9.0"]
                 [io.pedestal/pedestal.service "0.5.3"] 
                 [io.pedestal/pedestal.route "0.5.3"]
                 [io.pedestal/pedestal.jetty "0.5.3"]
                 [io.pedestal/pedestal.log "0.5.3"]
                 [org.slf4j/slf4j-simple "1.7.21"]
                 [reloaded.repl "0.2.3"]
                 [com.stuartsierra/component "0.3.2"]
                 [environ "1.1.0"]
                 [cljs-http "0.1.44"]]

  :plugins [[lein-figwheel "0.5.15"]
            [lein-garden "0.3.0"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src/clj" "src/cljs"]

  :garden {:builds [{:source-paths ["src/clj"]
                     :stylesheet messenjer.styles.core/app
                     :compiler {:output-to "resources/public/css/compiled/styles.css"                     
                                :pretty-print? false}}]}

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src/cljs"]
                :figwheel {:open-urls ["http://localhost:3449/index.html"]}

                :compiler {:main messenjer.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/messenjer.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/messenjer.js"
                           :main messenjer.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/public/css"]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.9"]
                                  [figwheel-sidecar "0.5.15"]
                                  [com.cemerick/piggieback "0.2.2"]]
                   :source-paths ["src" "dev"]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}})
