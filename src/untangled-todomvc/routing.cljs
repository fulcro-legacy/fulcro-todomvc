(ns untangled-todomvc.routing
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [om.next :as om])
  (:import goog.History))

(defn configure-routing! [reconciler]
  (secretary/set-config! :prefix "#")

  (defroute active-items "/active" []
    (om/transact! reconciler `[(todo/filter ~{:filter :active})]))

  (defroute completed-items "/completed" []
    (om/transact! reconciler `[(todo/filter ~{:filter :completed})]))

  (defroute all-items "*" []
    (om/transact! reconciler `[(todo/filter ~{:filter :none})])))

(defonce history
  (let [h (History.)]
    (events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
    (doto h (.setEnabled true))))
