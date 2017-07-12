(ns fulcro-todomvc.main
  (:require [fulcro-todomvc.ui :as ui]
            [fulcro-todomvc.core :as core]
            fulcro-todomvc.mutations
            [fulcro.client.core :as uc]))

(defonce mounted-app (reset! core/app (uc/mount @core/app ui/Root "app")))
