(ns untangled-todomvc.core
  (:require [untangled.client.core :as uc]
            [untangled-todomvc.ui :as ui]
            [untangled-todomvc.routing :refer [configure-routing!]]
            [untangled-todomvc.storage :as util]
            untangled-todomvc.mutations
            [goog.events :as events]
            [secretary.core :as secretary :refer-macros [defroute]]
            [goog.history.EventType :as EventType]
            [om.next :as om]
            [untangled.client.logging :as log])
  (:import goog.History))

(defonce app (atom (uc/new-untangled-client
                     :initial-state (if-let [storage (util/get-storage)]
                                      (om/db->tree (om/get-query ui/TodoList) storage storage)
                                      {:todos/filter :none})

                     ;; Setting an atom in initial state not working as expected for untangled-client
                     #_(if-let [storage (util/get-storage)]
                         (atom (log/debug "Storage" storage))
                         {})
                     :started-callback (fn [app]
                                         (log/set-level :none)
                                         (configure-routing! (:reconciler app))
                                         (let [h (History.)]
                                           (events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
                                           (doto h (.setEnabled true)))))))


