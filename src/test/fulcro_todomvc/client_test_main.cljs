(ns fulcro-todomvc.client-test-main
  (:require fulcro-todomvc.tests-to-run
            [fulcro-spec.selectors :as sel]
            [fulcro-spec.suite :as suite]))

(enable-console-print!)

(suite/def-test-suite client-tests {:ns-regex #".*todomvc..*-spec"}
  {:default   #{::sel/none :focused}
   :available #{:focused}})
