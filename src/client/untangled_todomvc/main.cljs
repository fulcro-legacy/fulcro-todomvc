(ns untangled-todomvc.main
  (:require [untangled-todomvc.ui :as ui]
            [untangled-todomvc.core :as core]
            untangled-todomvc.mutations
            [untangled.client.core :as uc]))

(defonce mounted-app (reset! core/app (uc/mount @core/app ui/Root "app")))
