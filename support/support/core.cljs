(ns support.core
  (:require [untangled.client.core :as uc]
            [om.next :as om]
            [support.ui :as ui]))

(defonce app (atom (uc/new-untangled-client
                     :initial-state {:react-key "sdf938"}
                     :started-callback (constantly nil))))


