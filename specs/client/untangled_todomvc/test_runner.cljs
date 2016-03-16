(ns ^:figwheel-always untangled-todomvc.test-runner
  (:require
    untangled-todomvc.tests-to-run
    [untangled-spec.reporters.suite :refer-macros [deftest-all-suite]]))

(deftest-all-suite todomvc-specs #".*-spec")

(def on-load todomvc-specs)

(todomvc-specs)
