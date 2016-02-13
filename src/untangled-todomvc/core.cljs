(ns untangled-todomvc.core
  (:require [untangled.client.core :as uc]
            [untangled-todomvc.ui :as ui]
            [untangled-todomvc.utils :as util]
            untangled-todomvc.mutations
            [cljs.pprint :refer [pprint]]
            [devtools.core :as devtools]))

(defonce cljs-build-tools
  (do (devtools/enable-feature! :sanity-hints)
      (devtools.core/install!)))

(enable-console-print!)

(defonce app (atom (uc/new-untangled-client
                     :initial-state (if-let [storage (util/get-storage)] storage {})
                     :started-callback (constantly nil))))

(reset! app (uc/mount @app ui/TodoList "app"))

