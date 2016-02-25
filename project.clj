(defproject untangled-todomvc "0.3.0-SNAPSHOT"
  :description "TodoMVC implemention using untangled.client"
  :url "http://www.thenavisway.com/"
  :license {:name "NAVIS"
            :url  "http://www.thenavisway.com"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.omcljs/om "1.0.0-alpha31-SNAPSHOT"]
                 [navis/untangled-client "0.4.4-SNAPSHOT"]
                 [navis/untangled-server "0.4.4-SNAPSHOT" :exclusions [io.aviso/pretty]]
                 [secretary "1.2.3"]
                 [joda-time "2.8.2"]
                 [clj-time "0.11.0"]
                 [commons-codec "1.10"]
                 [com.taoensso/timbre "4.2.1"]
                 [com.stuartsierra/component "0.3.0"]
                 [untangled-spec "0.3.3" :scope "test"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :source-paths ["dev/server" "src" "specs" "checkouts/untangled-server/src"]

  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["dev/client" "src" "checkouts/untangled-client/src"]
                        :figwheel     true
                        :compiler     {:main          "untangled-todomvc.core"
                                       :asset-path    "js/compiled/dev"
                                       :output-to     "resources/public/js/compiled/untangled-todomvc.js"
                                       :output-dir    "resources/public/js/compiled/dev"
                                       :parallel-build true
                                       :recompile-dependents true
                                       :optimizations :none}}

                       {:id           "test"
                        :source-paths ["src" "specs" "checkouts/untangled-client/src"]
                        :figwheel     {:on-jsload "untangled-todomvc.test-runner/on-load"}
                        :compiler     {:main       "untangled-todomvc.test-runner"
                                       :asset-path "js/compiled/specs"
                                       :parallel-build true
                                       :recompile-dependents true
                                       :output-to  "resources/public/js/compiled/todomvc-specs.js"
                                       :output-dir "resources/public/js/compiled/specs"}}]}

  :figwheel {:css-dirs    ["resources/public/css"]
             :server-port 2345}

  :profiles {
             :dev {
                   :source-paths ["src" "specs" "dev/server"]
                   :repl-options {
                                  :init-ns          user
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]
                                  :port             7001
                                  }
                   :env          {:dev true}
                   :dependencies [[figwheel-sidecar "0.5.0-5" :exclusions [clj-time ring/ring-core commons-fileupload]]
                                  [binaryage/devtools "0.5.2" :exclusions [environ]]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.12"]]
                   }
             }

  :repositories [
                 ["releases" "https://artifacts.buehner-fry.com/artifactory/navis-maven-release"]
                 ["snapshots" "https://artifacts.buehner-fry.com/artifactory/navis-maven-snapshot"]
                 ["snapshots" "https://artifacts.buehner-fry.com/artifactory/internal-snapshots"]
                 ])
