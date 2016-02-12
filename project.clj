(defproject untangled-todomvc "0.2.0-SNAPSHOT"
  :description "TodoMVC implemention using untangled.client"
  :url "http://www.thenavisway.com/"
  :license {:name "NAVIS"
            :url  "http://www.thenavisway.com"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.omcljs/om "1.0.0-alpha30"]
                 [untangled-client "0.4.1"]
                 [binaryage/devtools "0.5.2"]
                 [untangled-spec "0.3.2" :scope "test"]]

  :plugins [[lein-figwheel "0.5.0-5"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :source-paths ["src" "specs"]

  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src"]
                        :figwheel     true
                        :compiler     {:main          "untangled-todomvc.core"
                                       :asset-path    "js/compiled/dev"
                                       :output-to     "resources/public/js/compiled/untangled-todomvc.js"
                                       :output-dir    "resources/public/js/compiled/dev"
                                       :optimizations :none}}

                       {:id           "test"
                        :source-paths ["src" "specs"]
                        :figwheel     {:on-jsload "untangled-todomvc.test-runner/on-load"}
                        :compiler     {:main       "untangled-todomvc.test-runner"
                                       :asset-path "js/compiled/specs"
                                       :output-to  "resources/public/js/compiled/todomvc-specs.js"
                                       :output-dir "resources/public/js/compiled/specs"}}]}

  :figwheel {:css-dirs    ["resources/public/css"]
             :server-port 2345}

  :repositories [["releases" "https://artifacts.buehner-fry.com/artifactory/release"]])
