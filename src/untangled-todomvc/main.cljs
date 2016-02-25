(ns untangled-todomvc.main
  (:require [untangled-todomvc.ui :as ui]
            [untangled-todomvc.core :as core]
            [untangled-todomvc.routing :refer [configure-routing!]]
            [untangled.client.core :as uc]
            [untangled.client.logging :as log]))

(log/set-level :none)
(reset! core/app (uc/mount @core/app ui/TodoList "app"))
(configure-routing! (:reconciler @core/app))
