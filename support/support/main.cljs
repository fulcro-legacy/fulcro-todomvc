(ns support.main
  (:require [support.ui :as ui]
            [support.core :as core]
            [untangled.client.logging :as log]
            [untangled.client.core :as uc]))

;(log/set-level :none)
(reset! core/app (uc/mount @core/app ui/Root "support"))

