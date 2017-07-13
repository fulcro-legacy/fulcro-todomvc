(ns ^:figwheel-always fulcro-todomvc.test-runner
  (:require
    fulcro-todomvc.tests-to-run
    [fulcro-spec.selectors :as sel]
    [fulcro-spec.suite :as suite]))

(suite/def-test-suite on-load {:ns-regex #"fulcro.todo.*-spec"}
  {:default #{::sel/none :focused}
   :available #{:focused :should-fail}})

