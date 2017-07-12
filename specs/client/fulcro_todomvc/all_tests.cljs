(ns fulcro-todomvc.all-tests
  (:require
    fulcro-todomvc.tests-to-run
    [doo.runner :refer-macros [doo-all-tests]]))

(doo-all-tests #".*-spec")
