(defproject untangled-todomvc "0.3.0-SNAPSHOT"
  :description "TodoMVC implemention using untangled.client"
  :url "http://www.thenavisway.com/"
  :license {:name "NAVIS"
            :url  "http://www.thenavisway.com"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.omcljs/om "1.0.0-alpha30"]
                 [navis/untangled-client "0.4.4"]
                 [secretary "1.2.3" :exclusions [com.cemerick/clojurescript.test]]
                 [navis/untangled-spec "0.3.5" :scope "test"]]

  :plugins [[lein-cljsbuild "1.1.2"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :source-paths ["dev/server" "src/client" "specs/client"]

  :cljsbuild {:builds {:dev        {:source-paths ["dev/client" "src/client"]
                                    :figwheel     true
                                    :compiler     {:main                 "cljs.user"
                                                   :asset-path           "js/compiled/dev"
                                                   :output-to            "resources/public/js/compiled/untangled-todomvc.js"
                                                   :output-dir           "resources/public/js/compiled/dev"
                                                   :parallel-build       true
                                                   :recompile-dependents true
                                                   :optimizations        :none}}
                       :test       {
                                    :source-paths ["src/client" "specs/client"]
                                    :figwheel     {:on-jsload "untangled-todomvc.test-runner/on-load"}
                                    :compiler     {:main                 "untangled-todomvc.test-runner"
                                                   :asset-path           "js/compiled/specs"
                                                   :parallel-build       true
                                                   :recompile-dependents true
                                                   :output-to            "resources/public/js/compiled/todomvc-specs.js"
                                                   :output-dir           "resources/public/js/compiled/specs"}}
                       :production {:source-paths ["src/client"]
                                    :compiler     {:verbose       true
                                                   :output-to     "resources/public/js/compiled/untangled-todomvc.min.js"
                                                   :output-dir    "resources/public/js/compiled"
                                                   :pretty-print  false
                                                   :externs       ["externs.js"]
                                                   :closure-defines {goog.DEBUG false}
                                                   :optimizations :advanced}}}}

  :figwheel {:css-dirs    ["resources/public/css"]
             :server-port 2345}

  :profiles {
             :dev {
                   :repl-options {
                                  :init-ns          user
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]
                                  :port             7001
                                  }
                   :env          {:dev true}
                   :dependencies [[figwheel-sidecar "0.5.0-5"]
                                  [binaryage/devtools "0.5.2"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.12"]]}})
