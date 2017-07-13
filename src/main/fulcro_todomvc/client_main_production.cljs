(ns fulcro-todomvc.client-main-production
  (:require [fulcro-todomvc.ui :as ui]
            [fulcro-todomvc.client-setup :as core]
            [fulcro.client.core :as uc]))

; see dev/user.cljs for the entry point during development. This is only used in production builds.
(defonce mounted-app (reset! core/app (uc/mount @core/app ui/Root "app")))
