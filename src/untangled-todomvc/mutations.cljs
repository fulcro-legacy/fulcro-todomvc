(ns untangled-todomvc.mutations
  (:require [untangled.client.mutations :as m]
            [untangled.dom :refer [unique-key]]
            [untangled-todomvc.storage :as storage]))

(defn save-state [state] (storage/set-storage! @state))

(defmethod m/mutate 'todo/new-item [{:keys [state]} _ {:keys [text]}]
  {:action (fn []
             (let [id (unique-key)]
               (swap! state #(-> %
                              (update :todos (fn [todos] ((fnil conj []) todos [:todo/by-id id])))
                              (assoc-in [:todo/by-id id] {:id id :text text})))
               (save-state state)))})

(defmethod m/mutate 'todo/toggle-complete [{:keys [state]} _ {:keys [id]}]
  {:action (fn []
             (swap! state (fn [st] (update-in st [:todo/by-id id :completed] #(not %))))
             (swap! state (fn [st] (update st :todos/num-completed
                                     (if (get-in st [:todo/by-id id :completed]) inc dec))))
             (save-state state))})

(defmethod m/mutate 'todo/edit [{:keys [state]} _ {:keys [id text]}]
  {:action (fn []
             (swap! state assoc-in [:todo/by-id id :text] text)
             (save-state state))})

(defmethod m/mutate 'todo/delete-item [{:keys [state]} _ {:keys [id]}]
  {:action (fn []
             (letfn [(remove-item [todos] (vec (remove #(= id (second %)) todos)))]

               (when (get-in @state [:todo/by-id id :completed])
                 (swap! state update :todos/num-completed dec))

               (swap! state #(-> %
                              (update :todos remove-item)
                              (update :todo/by-id dissoc id)))
               (save-state state)))})

(defmethod m/mutate 'todo/toggle-all [{:keys [state]} _ {:keys [all-completed?]}]
  {:action (fn []
             (letfn [(set-completed [val todos]
                       (into {} (map (fn [[k v]] [k (assoc v :completed val)]) todos)))]

               (if all-completed?
                 (swap! state #(-> %
                                (assoc :todos/num-completed 0)
                                (update :todo/by-id (partial set-completed false))))
                 (swap! state #(-> %
                                (assoc :todos/num-completed (count (:todos @state)))
                                (update :todo/by-id (partial set-completed true)))))

               (save-state state)))})

(defmethod m/mutate 'todo/clear-complete [{:keys [state]} _ _]
  {:action (fn []
             (let [todos (vals (get @state :todo/by-id))
                   completed-todo-ids (set (keep #(when (:completed %) (:id %)) todos))]

               (swap! state assoc :todos/num-completed 0)
               (swap! state (fn [st]
                              (-> st
                                (update :todos
                                  (fn [todos] (vec (remove #(contains? completed-todo-ids (second %)) todos))))
                                (update :todo/by-id
                                  (fn [todos] (into {}
                                                (remove (fn [[k _]] (contains? completed-todo-ids k)) todos)))))))
               (save-state state)))})

(defmethod m/mutate 'todo/filter [{:keys [state]} _ {:keys [filter]}]
  {:action (fn []
             (swap! state assoc :todos/filter filter)
             (save-state state))})
