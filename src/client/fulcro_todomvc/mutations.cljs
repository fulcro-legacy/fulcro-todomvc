(ns fulcro-todomvc.mutations
  (:require [fulcro.client.mutations :as m :refer [defmutation]]
            [fulcro.client.util :refer [unique-key]]
            [fulcro.support-viewer :as v]))

(defmutation toggle-support [ignored]
  (action [{:keys [state]}] (swap! state update :ui/support-visible not)))

(defmutation todo-new-item [{:keys [id text]}]
  (action [{:keys [state ast]}]
    (swap! state #(-> %
                    (update-in [:todos :list/items] (fn [todos] ((fnil conj []) todos [:todo/by-id id])))
                    (assoc-in [:todo/by-id id] {:db/id id :item/label text}))))
  (remote [{:keys [state ast]}] (assoc ast :params {:id id :text text :list (:list @state)})))

(defmutation todo-check [{:keys [id]}]
  (action [{:keys [state]}] (swap! state assoc-in [:todo/by-id id :item/complete] true))
  (remote [env] true))

(defmutation todo-uncheck [{:keys [id]}]
  (action [{:keys [state]}] (swap! state assoc-in [:todo/by-id id :item/complete] false))
  (remote [env] true))

(defmutation todo-edit [{:keys [id text]}]
  (action [{:keys [state]}] (swap! state assoc-in [:todo/by-id id :item/label] text))
  (remote [env] true))

(defmutation todo-delete-item [{:keys [id]}]
  (action [{:keys [state]}]
    (letfn [(remove-item [todos] (vec (remove #(= id (second %)) todos)))]
      (swap! state #(-> %
                      (update-in [:todos :list/items] remove-item)
                      (update :todo/by-id dissoc id)))))
  (remote [env] true))

(defn- set-completed [val todos]
  (into {} (map (fn [[k v]] [k (assoc v :item/complete val)]) todos)))

(defmutation todo-check-all [ignored]
  (action [{:keys [state]}] (swap! state update :todo/by-id (partial set-completed true)))
  (remote [{:keys [ast state]}] (assoc ast :params {:id (:list @state)})))

(defmutation todo-uncheck-all [ignored]
  (action [{:keys [state]}] (swap! state update :todo/by-id (partial set-completed false)))
  (remote [{:keys [ast state]}] (assoc ast :params {:id (:list @state)})))

(defmutation todo-clear-complete [_]
  (action [{:keys [state]}]
    (swap! state (fn [st]
                   (-> st
                     (update-in [:todos :list/items]
                       (fn [todos] (vec (remove (fn [[_ id]] (get-in @state [:todo/by-id id :item/complete] false)) todos))))))))
  (remote [{:keys [ast state]}] (assoc ast :params {:id (:list @state)})))

(defmutation todo-filter [{:keys [filter]}]
  (action [{:keys [state]}] (swap! state assoc-in [:todos :list/filter] filter)))
