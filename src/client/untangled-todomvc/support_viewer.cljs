(ns untangled-todomvc.support-viewer
  (:require [untangled.client.core :as core]
            [untangled.support-viewer :as viewer]
            [untangled-todomvc.ui :as ui]
            [devtools.core :as devtools]))

(defonce cljs-build-tools
  (do (devtools/enable-feature! :sanity-hints)
      (devtools.core/install!)))

(defonce support-viewer
  (viewer/start-untangled-support-viewer "support" ui/Root "app"))

(core/refresh support-viewer)
