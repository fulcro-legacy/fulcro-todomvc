(ns untangled-todomvc.core
  (:require [untangled.client.core :as uc]
            [untangled-todomvc.ui :as ui]
            [untangled-todomvc.storage :as storage]
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

(defonce app (atom (uc/new-untangled-client
                     :initial-state {:list  (or (get-url-param "list") "main")
                                     :todos {:list/title  ""
                                             :list/items  []
                                             :list/filter :none}}

                     ;; Setting an atom in initial state not working as expected for untangled-client
                     #_(if-let [storage (storage/get-storage)]
                         (atom (log/debug "Storage" storage))
                         {})
                     :started-callback (fn [app]
                                         (let [reconciler (:reconciler app)
                                               state (om/app-state reconciler)
                                               list (:list @state)]
                                           (log/set-level :debug)
                                           (df/load-collection reconciler (om/get-query ui/Root) :params {:list list} :without #{:react-key})
                                           (configure-routing! reconciler))
                                         (let [h (History.)]
                                           (events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
                                           (doto h (.setEnabled true)))))))


