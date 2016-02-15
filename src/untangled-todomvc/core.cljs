(ns untangled-todomvc.core
  (:require [untangled.client.core :as uc]
            [untangled-todomvc.ui :as ui]
            [untangled-todomvc.utils :as util]
            untangled-todomvc.mutations
            [cljs.pprint :refer [pprint]]
            [devtools.core :as devtools]
            [untangled.client.logging :as log]
            [om.next :as om]))

(defonce cljs-build-tools
  (do (devtools/enable-feature! :sanity-hints)
      (devtools.core/install!)))

(enable-console-print!)

(log/set-level :debug)

(defonce app (atom (uc/new-untangled-client
                     :initial-state (if-let [storage (util/get-storage)]
                                      (om/db->tree (om/get-query ui/TodoList) storage storage)
                                      {:todos/filter :none})

                     ;; Setting an atom in initial state not working as expected for untangled-client
                     #_(if-let [storage (util/get-storage)]
                                      (atom (log/debug "Storage" storage))
                                      {})
                     :started-callback (constantly nil))))

(reset! app (uc/mount @app ui/TodoList "app"))
