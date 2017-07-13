(defproject fulcro-todomvc "1.0.3-SNAPSHOT"
  :description "TodoMVC implemention using Fulcro"
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/clojurescript "1.9.671"]
                 [org.omcljs/om "1.0.0-beta1"]
                 [fulcrologic/fulcro "1.0.0-beta2"]
                 [fulcrologic/fulcro-datomic "1.0.0-SNAPSHOT" :exclusions [org.clojure/tools.cli]]
                 [com.datomic/datomic-free "0.9.5561" :exclusions [com.google.guava/guava]]
                 [secretary "1.2.3" :exclusions [com.cemerick/clojurescript.test]]
                 [joda-time "2.9.9"]
                 [clj-time "0.13.0"]
                 [lein-doo "0.1.7" :scope "test" :exclusions [org.clojure/tools.reader]]
                 [org.clojure/tools.namespace "0.3.0-alpha4"]
                 [commons-codec "1.10"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.stuartsierra/component "0.3.2"]
                 [fulcrologic/fulcro-spec "1.0.0-beta2" :scope "test"]]

  :plugins [[lein-cljsbuild "1.1.6"]
            [lein-doo "0.1.7" :exclusions [org.clojure/tools.reader]]]

  :doo {:build "automated-tests"
        :paths {:karma "node_modules/karma/bin/karma"}}

  :fulcro-i18n {:default-locale        "en-US"
                :translation-namespace fulcro-todomvc.i18n
                :source-folder         "src/client"
                :target-build          "i18n"}

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target" "i18n/out"]
  :source-paths ["dev/server" "dev/watcher" "src/client" "src/server" "specs/client" "specs/server"]

  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["dev/client" "src/client"]
                        :figwheel     true
                        :compiler     {:main                 "cljs.user"
                                       :asset-path           "js/compiled/dev"
                                       :output-to            "resources/public/js/compiled/fulcro-todomvc.js"
                                       :output-dir           "resources/public/js/compiled/dev"
                                       :recompile-dependents true
                                       :optimizations        :none}}
                       {:id           "i18n"
                        :source-paths ["src/client"]
                        :compiler     {:main          "fulcro-todomvc.main"
                                       :output-to     "i18n/out/compiled.js"
                                       :output-dir    "i18n/out"
                                       :optimizations :whitespace}}
                       {:id           "test"
                        :source-paths ["src/client" "specs/client"]
                        :figwheel     {:on-jsload fulcro-todomvc.test-runner/on-load}
                        :compiler     {:main                 "fulcro-todomvc.test-runner"
                                       :asset-path           "js/test"
                                       :recompile-dependents true
                                       :output-to            "resources/public/js/test/test.js"
                                       :output-dir           "resources/public/js/test"}}
                       {:id           "automated-tests"
                        :source-paths ["src/client" "specs/client"]
                        :compiler     {:output-to     "resources/private/js/unit-tests.js"
                                       :main          fulcro-todomvc.all-tests
                                       :asset-path    "js"
                                       :output-dir    "resources/private/js"
                                       :optimizations :none}}
                       {:id           "support"
                        :source-paths ["src/client"]
                        :figwheel     true
                        :compiler     {:main                 "fulcro-todomvc.support-viewer"
                                       :asset-path           "js/compiled/support"
                                       :output-to            "resources/public/js/compiled/support.js"
                                       :output-dir           "resources/public/js/compiled/support"
                                       :recompile-dependents true
                                       :optimizations        :none}}

                       {:id           "production"
                        :source-paths ["src/client"]
                        :compiler     {:verbose         true
                                       :output-to       "resources/public/js/compiled/fulcro-todomvc.min.js"
                                       :output-dir      "resources/public/js/compiled"
                                       :pretty-print    false
                                       :closure-defines {goog.DEBUG false}
                                       :source-map      "resources/public/js/compiled/fulcro-todomvc.min.js.map"
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
                   :dependencies [[figwheel-sidecar "0.5.11" :exclusions [ring/ring-core]]
                                  [binaryage/devtools "0.9.4" :exclusions [environ]]
                                  [com.cemerick/piggieback "0.2.2"]
                                  [org.clojure/tools.nrepl "0.2.13"]]}})
