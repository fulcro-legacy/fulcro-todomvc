(ns ^:figwheel-always fulcro-todomvc.test-runner
  (:require
    fulcro-todomvc.tests-to-run
    [fulcro-spec.reporters.suite :refer-macros [deftest-all-suite]]))

(deftest-all-suite todomvc-specs #".*-spec")

(def on-load todomvc-specs)

(todomvc-specs)
