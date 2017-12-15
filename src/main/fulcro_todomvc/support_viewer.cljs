(ns fulcro-todomvc.support-viewer
  (:require
    [fulcro-todomvc.ui :as ui]
    [fulcro.client :as core]
    yahoo.intl-messageformat-with-locales
    [fulcro.support-viewer :as support]))

(defonce support-viewer
  (support/start-fulcro-support-viewer "support" ui/Root "app"))

(core/refresh support-viewer)
