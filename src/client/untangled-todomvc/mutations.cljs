(ns untangled-todomvc.mutations
  (:require [untangled.client.mutations :as m]
            [untangled.dom :refer [unique-key]]
            [untangled-todomvc.storage :as storage]
            [untangled-todomvc.history :as history]
            [untangled-todomvc.core :as core]))

(defmethod m/post-mutate :default [{:keys [state]} _ _] (storage/set-storage! @state))

(defmethod m/mutate 'support-viewer/send-support-request [{:keys [ast]} k p]
  {:remote (assoc ast :params {:comment "Howdy" :history (history/serialize-history @core/app)})})

(defmethod m/mutate 'todo/new-item [{:keys [state ast]} _ {:keys [id text]}]
  {:remote (assoc ast :params {:id id :text text :list (:list @state)})
   :action (fn []
             (swap! state #(-> %
                            (update :todos (fn [todos] ((fnil conj []) todos [:todo/by-id id])))
                            (assoc-in [:todo/by-id id] {:db/id id :item/label text}))))})

(defmethod m/mutate 'todo/check [{:keys [state]} _ {:keys [id]}]
  {:remote true
   :action (fn [] (swap! state assoc-in [:todo/by-id id :item/complete] true))})

(defmethod m/mutate 'todo/uncheck [{:keys [state]} _ {:keys [id]}]
  {:remote true
   :action (fn [] (swap! state assoc-in [:todo/by-id id :item/complete] false))})

(defmethod m/mutate 'todo/edit [{:keys [state]} _ {:keys [id text]}]
  {:remote true
   :action (fn [] (swap! state assoc-in [:todo/by-id id :text] text))})

(defmethod m/mutate 'todo/delete-item [{:keys [state]} _ {:keys [id]}]
  {:remote true
   :action (fn []
             (letfn [(remove-item [todos] (vec (remove #(= id (second %)) todos)))]

               (when (get-in @state [:todo/by-id id :completed])
                 (swap! state update :todos/num-completed dec))

               (swap! state #(-> %
                              (update :todos remove-item)
                              (update :todo/by-id dissoc id)))))})

(defmethod m/mutate 'todo/toggle-all [{:keys [state]} _ {:keys [all-completed?]}]
  {:remote true
   :action (fn []
             (letfn [(set-completed [val todos]
                       (into {} (map (fn [[k v]] [k (assoc v :completed val)]) todos)))]

               (if all-completed?
                 (swap! state #(-> %
                                (assoc :todos/num-completed 0)
                                (update :todo/by-id (partial set-completed false))))
                 (swap! state #(-> %
                                (assoc :todos/num-completed (count (:todos @state)))
                                (update :todo/by-id (partial set-completed true)))))))})

(defmethod m/mutate 'todo/clear-complete [{:keys [state]} _ _]
  {:remote true
   :action (fn []
             (let [todos (vals (get @state :todo/by-id))
                   completed-todo-ids (set (keep #(when (:completed %) (:id %)) todos))]

               (swap! state assoc :todos/num-completed 0)
               (swap! state (fn [st]
                              (-> st
                                (update :todos
                                  (fn [todos] (vec (remove #(contains? completed-todo-ids (second %)) todos))))
                                (update :todo/by-id
                                  (fn [todos] (into {}
                                                (remove (fn [[k _]] (contains? completed-todo-ids k)) todos)))))))))})

(defmethod m/mutate 'todo/filter [{:keys [ast state]} _ {:keys [filter]}]
  {:remote (update ast :params assoc :list (:list @state))
   :action (fn [] (swap! state assoc-in [:todos :list/filter] filter))})
