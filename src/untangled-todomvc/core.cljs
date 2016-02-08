(ns untangled-todomvc.core
  (:require [untangled.client.core :as uc]
            [untangled-todomvc.ui :as ui]))

(defonce app (uc/new-untangled-client))

(uc/mount app ui/Root "app")

(js/console.log "Untangled Todo with hot-code reload!")