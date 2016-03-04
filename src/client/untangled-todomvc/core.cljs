(ns untangled-todomvc.core
  (:require [untangled.client.core :as uc]
            [untangled-todomvc.ui :as ui]
            [untangled-todomvc.routing :refer [configure-routing!]]
            [goog.events :as events]
            [secretary.core :as secretary :refer-macros [defroute]]
            [goog.history.EventType :as EventType]
            [om.next :as om]
            [untangled.client.logging :as log]
            [untangled.client.data-fetch :as df])
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
    (df/load-collection reconciler (om/get-query ui/Root) :params {:list list}
                        :without #{:list/filter :ui/support-visible :react-key :app/locale})
    (configure-routing! reconciler))
  (let [h (History.)]
    (events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
    (doto h (.setEnabled true))))

(defonce app (atom (uc/new-untangled-client
                     :initial-state {:list  (or (get-url-param "list") "main")
                                     :todos {:list/title  ""
                                             :list/items  []
                                             :list/filter :none}}
                     :started-callback on-app-started)))


