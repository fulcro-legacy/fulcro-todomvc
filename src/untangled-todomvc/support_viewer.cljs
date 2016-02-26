(ns untangled-todomvc.support-viewer
  (:require [untangled.client.core :as core]
            [untangled.support-viewer :as viewer]
            [untangled-todomvc.ui :as ui]
            [untangled-todomvc.history :as history]))

(defonce support-viewer
  (viewer/start-untangled-support-viewer "support" ui/TodoList "app" (history/get-storage)))

(core/refresh support-viewer)
