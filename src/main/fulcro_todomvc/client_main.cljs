(ns fulcro-todomvc.client-main
  (:require [fulcro-todomvc.ui :as ui]
            [fulcro-todomvc.client-setup :as core]
            [fulcro.client :as fc]))

; see dev/user.cljs for the entry point during development. This is only used in production builds.
(defonce mounted-app (reset! core/app (fc/mount @core/app ui/Root "app")))
