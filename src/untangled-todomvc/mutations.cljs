(ns untangled-todomvc.mutations
  (:require [untangled.client.mutations :as m]))

(defmethod m/mutate 'todo/new-item [{:keys [state]} _ {:keys [text]}]
  {:action (fn [])})
