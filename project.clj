(defproject untangled-todomvc "1.0.3-SNAPSHOT"
  :description "TodoMVC implemention using untangled.client"
  :url "http://www.thenavisway.com/"
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.51"]
                 [org.omcljs/om "1.0.0-alpha40"]
                 [navis/untangled-client "0.5.3"]
                 [navis/untangled-server "0.6.1" :exclusions [io.aviso/pretty org.clojure/clojurescript]]
                 [navis/untangled-datomic "0.4.9" :exclusions [org.clojure/tools.cli]]
                 [com.datomic/datomic-free "0.9.5359" :exclusions [com.google.guava/guava]]
                 [secretary "1.2.3" :exclusions [com.cemerick/clojurescript.test]]
                 [joda-time "2.9.3"]
                 [clj-time "0.11.0"]
                 [lein-doo "0.1.6" :scope "test" :exclusions [org.clojure/tools.reader]]
                 [org.clojure/tools.namespace "0.2.11"]
                 [commons-codec "1.10"]
                 [com.taoensso/timbre "4.3.1"]
                 [com.stuartsierra/component "0.3.1"]
                 [navis/untangled-spec "0.3.6" :scope "test"]]

  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-doo "0.1.6" :exclusions [org.clojure/tools.reader]]
            [navis/untangled-lein-i18n "0.1.2" :exclusions [org.codehaus.plexus/plexus-utils org.clojure/tools.cli org.apache.maven.wagon/wagon-provider-api]]]

  :doo {:build "automated-tests"
        :paths {:karma "node_modules/karma/bin/karma"}}

  :untangled-i18n {:default-locale        "en-US"
                   :translation-namespace untangled-todomvc.i18n
                   :source-folder         "src/client"
                   :target-build          "i18n"
                   }

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target" "i18n/out"]
  :source-paths ["dev/server" "dev/watcher" "src/client" "src/server" "specs/client" "specs/server"]

  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["dev/client" "src/client"]
                        :figwheel     true
                        :compiler     {:main                 "cljs.user"
                                       :asset-path           "js/compiled/dev"
                                       :output-to            "resources/public/js/compiled/untangled-todomvc.js"
                                       :output-dir           "resources/public/js/compiled/dev"
                                       :recompile-dependents true
                                       :optimizations        :none}}
                       {:id           "i18n"
                        :source-paths ["src/client"]
                        :compiler     {:main          "untangled-todomvc.main"
                                       :output-to     "i18n/out/compiled.js"
                                       :output-dir    "i18n/out"
                                       :optimizations :whitespace}}
                       {:id           "test"
                        :source-paths ["src/client" "specs/client"]
                        :figwheel     {:on-jsload "untangled-todomvc.test-runner/on-load"}
                        :compiler     {:main                 "untangled-todomvc.test-runner"
                                       :asset-path           "js/compiled/specs"
                                       :recompile-dependents true
                                       :output-to            "resources/public/js/compiled/todomvc-specs.js"
                                       :output-dir           "resources/public/js/compiled/specs"}}
                       {:id           "automated-tests"
                        :source-paths ["src/client" "specs/client"]
                        :compiler     {:output-to     "resources/private/js/unit-tests.js"
                                       :main          untangled-todomvc.all-tests
                                       :asset-path    "js"
                                       :output-dir    "resources/private/js"
                                       :optimizations :none}}
                       {:id           "support"
                        :source-paths ["src/client"]
                        :figwheel     true
                        :compiler     {:main                 "untangled-todomvc.support-viewer"
                                       :asset-path           "js/compiled/support"
                                       :output-to            "resources/public/js/compiled/support.js"
                                       :output-dir           "resources/public/js/compiled/support"
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
                   :dependencies [[figwheel-sidecar "0.5.3-1" :exclusions [ring/ring-core]]
                                  [juxt/dirwatch "0.2.3"]
                                  [binaryage/devtools "0.6.1" :exclusions [environ]]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.12"]]}})
