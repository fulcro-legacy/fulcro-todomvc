(ns untangled-todomvc.core
  (:require [untangled.client.core :as uc]
            [untangled-todomvc.ui :as ui]
            [devtools.core :as devtools]))

(devtools/enable-feature! :sanity-hints)
(devtools.core/install!)

(defonce app (uc/new-untangled-client
               :initial-state {}
               :started-callback (constantly nil)))

(uc/mount app ui/Root "app")
