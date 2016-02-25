(ns cljs.user
  (:require
    [cljs.pprint :refer [pprint]]
    [devtools.core :as devtools]
    [untangled-todomvc.core :as core]
    [untangled.client.logging :as log]
    [untangled-todomvc.ui :as ui]
    [untangled-todomvc.routing :refer [configure-routing!]]
    [untangled.client.core :as uc]
    [cljs.reader :as reader]))

(enable-console-print!)

(defonce cljs-build-tools
  (do (devtools/enable-feature! :sanity-hints)
      (devtools.core/install!)))

(log/set-level :debug)

(reset! core/app (uc/mount @core/app ui/TodoList "app"))

(defonce routing
  (configure-routing! (:reconciler @core/app)))

(defn log-app-state
  "Helper for logging the app-state, pass in top-level keywords from the app-state and it will print only those
  keys and their values."
  [& keywords]
  (pprint (let [app-state @(:reconciler @core/app)]
            (if (= 0 (count keywords))
              app-state
              (select-keys app-state keywords)))))

(def storage-key "app-history")

(defn get-storage []
  ((fnil reader/read-string "") (.getItem js/localStorage storage-key)))

(defn set-storage! [val]
  (->> val pr-str (.setItem js/localStorage storage-key)))

(defn serialize-history [untangled-app]
  (let [history-steps (-> @untangled-todomvc.core/app :reconciler :config :history .-arr)
        history-map (-> @untangled-todomvc.core/app :reconciler :config :history .-index deref)]
    {:steps   history-steps
     :history history-map}))

(comment
  (set-storage! (serialize-history untangled-todomvc.core/app))

  )
