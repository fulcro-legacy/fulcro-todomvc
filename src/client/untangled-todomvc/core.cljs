(ns untangled-todomvc.core
  (:require [untangled.client.core :as uc]
            [untangled-todomvc.ui :as ui]
            [untangled-todomvc.storage :as storage]
            [untangled-todomvc.routing :refer [configure-routing!]]
            [goog.events :as events]
            [secretary.core :as secretary :refer-macros [defroute]]
            [goog.history.EventType :as EventType]
            [om.next :as om]
            [untangled.client.logging :as log])
  (:import goog.History))

(defonce app (atom (uc/new-untangled-client
                     :initial-state (if-let [storage (storage/get-storage)]
                                      (om/db->tree (om/get-query ui/TodoList) storage storage)
                                      {:todos/filter :none})
                     :started-callback (fn [app]
                                         (configure-routing! (:reconciler app))
                                         (let [h (History.)]
                                           (events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
                                           (doto h (.setEnabled true)))))))


