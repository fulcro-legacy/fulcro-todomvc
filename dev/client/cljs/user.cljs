(ns cljs.user
  (:require
    [cljs.pprint :refer [pprint]]
    [fulcro-todomvc.client-setup :as core]
    [fulcro.client.logging :as log]
    [fulcro-todomvc.ui :as ui]
    [fulcro.client.core :as uc]))

(enable-console-print!)
(log/set-level :debug)

(reset! core/app (uc/mount @core/app ui/Root "app"))

(defn log-app-state
  "Helper for logging the app-state, pass in top-level keywords from the app-state and it will print only those
  keys and their values."
  [& keywords]
  (pprint (let [app-state @(:reconciler @core/app)]
            (if (= 0 (count keywords))
              app-state
              (select-keys app-state keywords)))))


