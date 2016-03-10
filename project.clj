(defproject untangled-todomvc "1.0.0"
  :description "TodoMVC implemention using untangled.client"
  :url "http://www.thenavisway.com/"
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.omcljs/om "1.0.0-alpha31"]
                 [org.clojure/tools.cli "0.3.1"]
                 [navis/untangled-client "0.4.5"]
                 [navis/untangled-server "0.4.5" :exclusions [io.aviso/pretty org.clojure/clojurescript]]
                 [navis/untangled-datomic "0.4.4"]
                 [com.datomic/datomic-free "0.9.5350"]
                 [secretary "1.2.3" :exclusions [com.cemerick/clojurescript.test]]
                 [joda-time "2.8.2"]
                 [clj-time "0.11.0"]
                 [commons-codec "1.10"]
                 [com.taoensso/timbre "4.3.1"]
                 [com.stuartsierra/component "0.3.1"]
                 [navis/untangled-spec "0.3.5" :scope "test"]]

  :plugins [[lein-cljsbuild "1.1.2"]
            [navis/untangled-lein-i18n "0.1.2" :exclusions [org.codehaus.plexus/plexus-utils]]]

  :untangled-i18n {:default-locale        "en-US"
                   :translation-namespace untangled-todomvc.i18n
                   :source-folder         "src/client"
                   :target-build          "i18n"
                   }

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target" "i18n/out"]
  :source-paths ["dev/server" "src/client" "src/server" "specs/client" "specs/server"]

  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["dev/client" "src/client"]
                        :figwheel     true
                        :compiler     {:main                 "cljs.user"
                                       :asset-path           "js/compiled/dev"
                                       :output-to            "resources/public/js/compiled/untangled-todomvc.js"
                                       :output-dir           "resources/public/js/compiled/dev"
                                       :parallel-build       true
                                       :recompile-dependents true
                                       :optimizations        :none}}
                       {:id           "i18n"
                        :source-paths ["src/client"]
                        :compiler     {:main           "untangled-todomvc.main"
                                       :output-to      "i18n/out/compiled.js"
                                       :output-dir     "i18n/out"
                                       :parallel-build true
                                       :optimizations  :whitespace}}
                       {:id           "test"
                        :source-paths ["src/client" "specs/client"]
                        :figwheel     {:on-jsload "untangled-todomvc.test-runner/on-load"}
                        :compiler     {:main                 "untangled-todomvc.test-runner"
                                       :asset-path           "js/compiled/specs"
                                       :parallel-build       true
                                       :recompile-dependents true
                                       :output-to            "resources/public/js/compiled/todomvc-specs.js"
                                       :output-dir           "resources/public/js/compiled/specs"}}
                       {:id           "support"
                        :source-paths ["src/client"]
                        :figwheel     true
                        :compiler     {:main                 "untangled-todomvc.support-viewer"
                                       :asset-path           "js/compiled/support"
                                       :output-to            "resources/public/js/compiled/support.js"
                                       :output-dir           "resources/public/js/compiled/support"
                                       :parallel-build       true
                                       :recompile-dependents true
                                       :optimizations        :none}}

                       {:id           "production"
                        :source-paths ["src/client"]
                        :compiler     {:verbose         true
                                       :output-to       "resources/public/js/compiled/untangled-todomvc.min.js"
                                       :output-dir      "resources/public/js/compiled"
                                       :pretty-print    false
                                       :closure-defines {goog.DEBUG false}
                                       :source-map      "resources/public/js/compiled/untangled-todomvc.min.js.map"
                                       :elide-asserts   true
                                       :optimizations   :advanced}}]}

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
                   :dependencies [[figwheel-sidecar "0.5.0-5" :exclusions [clj-time ring/ring-core commons-fileupload]]
                                  [binaryage/devtools "0.5.2" :exclusions [environ]]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.12"]]}})
