(ns untangled-todomvc.all-tests
  (:require
    untangled-todomvc.tests-to-run
    [doo.runner :refer-macros [doo-all-tests]]))

(doo-all-tests #".*-spec")
