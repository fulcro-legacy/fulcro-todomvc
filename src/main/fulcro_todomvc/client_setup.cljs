(ns fulcro-todomvc.client-setup
  (:require [fulcro.client :as fc]
            [fulcro-todomvc.ui :as ui]
            [goog.events :as events]
            [secretary.core :as secretary :refer-macros [defroute]]
            [goog.history.EventType :as EventType]
            [fulcro-todomvc.api :as m]                      ; ensures mutations are loaded
            [fulcro.client.mutations :refer [defmutation mutate]]
            [fulcro.client.primitives :as prim]
            fulcro-todomvc.i18n.locales                     ; make sure i18n resources are loaded
            fulcro-todomvc.i18n.default-locale
            [fulcro.client.data-fetch :as df])
  (:import goog.History))

;; On page load these will get triggered, but the list won't be loaded yet. So, see the code for todo-filter and
;; set-desired-filter (post mutation on the load)
(defn configure-routing! [reconciler]
  (secretary/set-config! :prefix "#")

  ; Secretary could fire an event *while* we're in setup, so we delay txes
  (defroute active-items "/active" []
    (prim/transact! reconciler `[(m/todo-filter {:filter :list.filter/active})]))

  (defroute completed-items "/completed" []
    (prim/transact! reconciler `[(m/todo-filter {:filter :list.filter/completed})]))

  (defroute all-items "*" []
    (prim/transact! reconciler `[(m/todo-filter {:filter :list.filter/none})])))

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

(defn on-app-started
  "Stuff we do on initial load. Given to new-fulcro-client."
  [app]
  (let [reconciler (:reconciler app)
        state      (prim/app-state reconciler)
        list       (or (get-url-param "list") "My List")]   ; the list name is in the URI, or defaults to "My List"
    (swap! state assoc :list list)
    ; load the list (which will auto-create it on the server). The post mutation
    ; ensures that HTML5 routing gets applied. (that event will have completed before the list is loaded, so the filtering
    ; would get lost).
    (df/load app :todos ui/TodoList {:without       #{:list/filter} :params {:list list}
                                     :post-mutation `m/set-desired-filter
                                     :target        [:application :root :todos]})
    (configure-routing! reconciler))
  ; Start up the HTML5 history events, and dispatch them through secretary. See routes at top of this file.
  (let [h (History.)]
    (events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
    (doto h (.setEnabled true))))

; the defonce is so we get hot code reload
(defonce app (atom (fc/new-fulcro-client
                     :started-callback on-app-started)))
