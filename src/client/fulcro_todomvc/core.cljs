(ns fulcro-todomvc.core
  (:require [fulcro.client.core :as uc]
            [fulcro-todomvc.ui :as ui]
            [fulcro-todomvc.routing :refer [configure-routing!]]
            [goog.events :as events]
            [secretary.core :as secretary :refer-macros [defroute]]
            [goog.history.EventType :as EventType]
            [om.next :as om]
            fulcro-todomvc.i18n.locales
            fulcro-todomvc.i18n.default-locale
            [fulcro.client.logging :as log]
            [fulcro.client.data-fetch :as df])
  (:import goog.History))

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
        state (om/app-state reconciler)
        list (:list @state)]
    (df/load app :todos (om/get-query ui/TodoList) {:without #{:list/filter} :params {:todos {:list list}}})
    (configure-routing! reconciler))
  (let [h (History.)]
    (events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
    (doto h (.setEnabled true))))

(defonce app (atom (uc/new-fulcro-client
                     :initial-state {:list  (or (get-url-param "list") "main")
                                     :todos {:list/title  ""
                                             :list/items  []
                                             :list/filter :none}}
                     :started-callback on-app-started)))


