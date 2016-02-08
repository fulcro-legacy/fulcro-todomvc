(defproject untangled-todomvc "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.omcljs/om "1.0.0-alpha30"]
                 [untangled-client "0.4.2-SNAPSHOT"]
                 [binaryage/devtools "0.5.2"]]
  :plugins [[lein-figwheel "0.5.0-5"]]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src"]
                        :figwheel     true
                        :compiler     {:main          "untangled-todomvc.core"
                                       :asset-path    "js/compiled/dev"
                                       :output-to     "resources/public/js/compiled/untangled-todomvc.js"
                                       :output-dir    "resources/public/js/compiled/dev"
                                       :optimizations :none}}]}
  :figwheel {:css-dirs ["resources/public/css"]
             :server-port 2345})
