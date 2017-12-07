(ns fulcro-todomvc.support-viewer
  (:require [fulcro.client :as core]
            [fulcro.support-viewer :as viewer]
            [fulcro-todomvc.ui :as ui]))

(defonce support-viewer
  (viewer/start-fulcro-support-viewer "support" ui/Root "app"))

(core/refresh support-viewer)
