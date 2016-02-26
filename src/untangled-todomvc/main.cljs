(ns untangled-todomvc.main
  (:require [untangled-todomvc.ui :as ui]
            [untangled-todomvc.core :as core]
            [untangled.client.core :as uc]))

(reset! core/app (uc/mount @core/app ui/TodoList "app"))
