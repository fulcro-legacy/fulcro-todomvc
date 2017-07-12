(ns fulcro-todomvc.support-viewer
  (:require [fulcro.client.core :as core]
            [fulcro.support-viewer :as viewer]
            [fulcro-todomvc.ui :as ui]
            [devtools.core :as devtools]))

(defonce cljs-build-tools
  (do (devtools/enable-feature! :sanity-hints)
      (devtools.core/install!)))

(defonce support-viewer
  (viewer/start-fulcro-support-viewer "support" ui/Root "app"))

(core/refresh support-viewer)
