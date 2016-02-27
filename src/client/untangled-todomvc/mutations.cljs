(ns untangled-todomvc.mutations
  (:require [untangled.client.mutations :as m]
            [untangled.dom :refer [unique-key]]
            [untangled-todomvc.storage :as storage]
            [untangled-todomvc.history :as history]
            [untangled-todomvc.core :as core]))

(defmethod m/post-mutate :default [{:keys [state]} _ _] (storage/set-storage! @state))

(defmethod m/mutate 'support-viewer/send-support-request [{:keys [ast]} k p]
  {:remote (assoc ast :params {:comment "Howdy" :history (history/serialize-history @core/app)})})

(defmethod m/mutate 'todo/new-item [{:keys [state]} _ {:keys [text]}]
  {:action (fn []
             (let [id (unique-key)]
               (swap! state #(-> %
                              (update :todos (fn [todos] ((fnil conj []) todos [:todo/by-id id])))
                              (assoc-in [:todo/by-id id] {:id id :text text})))))})

(defmethod m/mutate 'todo/toggle-complete [{:keys [state]} _ {:keys [id]}]
  {:action (fn [] (swap! state (fn [st] (update-in st [:todo/by-id id :completed] #(not %)))))})

(defmethod m/mutate 'todo/edit [{:keys [state]} _ {:keys [id text]}]
  {:action (fn [] (swap! state assoc-in [:todo/by-id id :text] text))})

(defmethod m/mutate 'todo/delete-item [{:keys [state]} _ {:keys [id]}]
  {:action (fn []
             (letfn [(remove-item [todos] (vec (remove #(= id (second %)) todos)))]

               (swap! state #(-> %
                              (update :todos remove-item)
                              (update :todo/by-id dissoc id)))))})

(defmethod m/mutate 'todo/toggle-all [{:keys [state]} _ {:keys [all-completed?]}]
  {:action (fn []
             (letfn [(set-completed [val todos]
                       (into {} (map (fn [[k v]] [k (assoc v :completed val)]) todos)))]

               (if all-completed?
                 (swap! state update :todo/by-id (partial set-completed false))
                 (swap! state update :todo/by-id (partial set-completed true)))))})

(defmethod m/mutate 'todo/clear-complete [{:keys [state]} _ _]
  {:action (fn []
             (let [todos (get @state :todo/by-id)
                   completed-todo-ids (set (keep (fn [[id data]] (when (:completed data) id)) todos))]

               (swap! state
                 (fn [st]
                   (-> st
                     (update :todos (fn [todos] (vec (remove #(contains? completed-todo-ids (second %)) todos))))
                     (update :todo/by-id
                       (fn [todos] (into {} (remove (fn [[k _]] (contains? completed-todo-ids k)) todos)))))))))})

(defmethod m/mutate 'todo/filter [{:keys [state]} _ {:keys [filter]}]
  {:action (fn [] (swap! state assoc :todos/filter filter))})
