(ns ^:figwheel-always untangled-todomvc.test-runner
  (:require
    untangled-todomvc.todo-spec
    untangled-todomvc.storage-spec
    [untangled-spec.reporters.suite :refer-macros [deftest-all-suite]]))

(deftest-all-suite todomvc-specs #".*-spec")

(def on-load todomvc-specs)

(todomvc-specs)
