(ns fulcro-todomvc.routing
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [fulcro-todomvc.mutations :as m]
            [om.next :as om]))

(defn configure-routing! [reconciler]
  (secretary/set-config! :prefix "#")

  (defroute active-items "/active" []
    (om/transact! reconciler `[(m/todo-filter {:filter :list.filter/active})]))

  (defroute completed-items "/completed" []
    (om/transact! reconciler `[(m/todo-filter {:filter :list.filter/completed})]))

  (defroute all-items "*" []
    (om/transact! reconciler `[(m/todo-filter {:filter :list.filter/none})])))

