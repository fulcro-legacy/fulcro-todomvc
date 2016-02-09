(ns untangled-todomvc.mutations
  (:require [untangled.client.mutations :as m]
            [om.next :as om]))

(defmethod m/mutate 'todo/new-item [{:keys [state]} _ {:keys [text]}]
  {:action (fn []
             (let [id (om/tempid)]
               (swap! state #(-> %
                              (update :todos (fn [todos] (conj todos [:todo/by-id id])))
                              (assoc-in [:todo/by-id id] {:id id :text text})))))})

(defmethod m/mutate 'todo/delete-item [{:keys [state]} _ {:keys [id]}]
  {:action (fn []
             (letfn [(remove-item [todos] (vec (remove #(= id (second %)) todos)))]
               (swap! state #(-> %
                              (update :todos remove-item)
                              (update :todo/by-id dissoc id)))))})