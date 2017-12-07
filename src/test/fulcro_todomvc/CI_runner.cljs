(ns fulcro-todomvc.CI-runner
  (:require
    fulcro-todomvc.tests-to-run
    [doo.runner :refer-macros [doo-all-tests]]))

;; This file is for running JS tests via karma/node for CI server
(doo-all-tests #".*-spec")
