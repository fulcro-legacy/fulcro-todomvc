(ns fulcro-todomvc.client-setup
  (:require [fulcro.client.core :as uc]
            [fulcro-todomvc.ui :as ui]
            [goog.events :as events]
            [secretary.core :as secretary :refer-macros [defroute]]
            [goog.history.EventType :as EventType]
            [fulcro-todomvc.api :as m]                      ; ensures mutations are loaded
            [fulcro.client.mutations :refer [defmutation mutate]]
            [fulcro.client.primitives :as om]
            fulcro-todomvc.i18n.locales                     ; make sure i18n resources are loaded
            fulcro-todomvc.i18n.default-locale
            [fulcro.client.data-fetch :as df])
  (:import goog.History))

(defn delayed-transact! [r tx]
  (js/setTimeout #(om/transact! r tx) 10))

(defn configure-routing! [reconciler]
  (secretary/set-config! :prefix "#")

  ; Secretary could fire an event *while* we're in setup, so we delay txes
  (defroute active-items "/active" []
    (delayed-transact! (om/app-root reconciler) `[(m/todo-filter {:filter :list.filter/active})]))

  (defroute completed-items "/completed" []
    (delayed-transact! (om/app-root reconciler) `[(m/todo-filter {:filter :list.filter/completed})]))

  (defroute all-items "*" []
    (delayed-transact! (om/app-root reconciler) `[(m/todo-filter {:filter :list.filter/none})])))

(defn get-url
  [] (-> js/window .-location .-href))

(defn uri-params
  ([] (uri-params (get-url)))
  ([url]
   (let [query-data (.getQueryData (goog.Uri. url))]
     (into {}
       (for [k (.getKeys query-data)]
         [k (.get query-data k)])))))

(defn get-url-param
  ([param-name] (get-url-param (get-url) param-name))
  ([url param-name]
   (get (uri-params url) param-name)))

(defn on-app-started [app]
  (let [reconciler (:reconciler app)
        state      (om/app-state reconciler)
        list       (or (get-url-param "list") "main")]
    (swap! state assoc :list list)
    (df/load app :todos ui/TodoList {:without #{:list/filter} :params {:list list}})
    (configure-routing! reconciler))
  (let [h (History.)]
    (events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
    (doto h (.setEnabled true))))

(defonce app (atom (uc/new-fulcro-client
                     :started-callback on-app-started)))

; support viewer mutation needs to be here, so app history can be resolved without a circular reference
(defmethod mutate 'support/send-request [{:keys [ast state]} _ {:keys [comment]}]
  {:remote (assoc ast :params {:comment comment :history (uc/history @app)})})
