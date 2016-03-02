(ns untangled-todomvc.routing
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [om.next :as om]))

(defn configure-routing! [reconciler]
  (secretary/set-config! :prefix "#")

  (defroute active-items "/active" []
    (om/transact! reconciler '[(todo/filter {:filter :active})]))

  (defroute completed-items "/completed" []
    (om/transact! reconciler '[(todo/filter {:filter :completed})]))

  (defroute all-items "*" []
    (om/transact! reconciler '[(todo/filter {:filter :none})])))

