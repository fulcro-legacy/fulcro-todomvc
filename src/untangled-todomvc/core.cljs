(ns untangled-todomvc.core
  (:require [untangled.client.core :as uc]
            [untangled-todomvc.ui :as ui]
            [untangled-todomvc.initial-state :as s]
            untangled-todomvc.mutations
            [cljs.pprint :refer [pprint]]
            [devtools.core :as devtools]))

(defonce cljs-build-tools
  (do (devtools/enable-feature! :sanity-hints)
      (devtools.core/install!)))

(enable-console-print!)

(defonce app (uc/new-untangled-client
               :initial-state s/initial-state
               :started-callback (constantly nil)))

(def initialized-app (uc/mount app ui/TodoList "app"))

(pprint @(:reconciler initialized-app))

