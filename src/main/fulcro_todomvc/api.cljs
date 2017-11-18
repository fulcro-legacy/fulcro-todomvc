(ns fulcro-todomvc.api
  (:require [fulcro.client.mutations :as m :refer [defmutation]]
            [fulcro.util :refer [unique-key]]
            [fulcro.support-viewer :as v]
            [fipp.edn :refer [pprint]]
            [fulcro.client.primitives :as prim]
            [fulcro.client.logging :as log]))

(defmutation toggle-support [ignored]
  (action [{:keys [state]}] (swap! state update :ui/support-visible not)))

(defmutation todo-new-item [{:keys [list-id id text]}]
  (action [{:keys [state ast]}]
    (swap! state #(-> %
                    (update-in [:list/by-id list-id :list/items] (fn [todos] ((fnil conj []) todos [:todo/by-id id])))
                    (assoc-in [:list/by-id list-id :ui/new-item-text] "")
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

(defn remove-from-idents [vec-of-idents id]
  (vec (filter (fn [ident] (not= id (second ident))) vec-of-idents)))

(defmutation todo-delete-item [{:keys [list-id id]}]
  (action [{:keys [state]}]
    (swap! state #(-> %
                    (update-in [:list/by-id list-id :list/items] remove-from-idents id)
                    (update :todo/by-id dissoc id))))
  (remote [env] true))

(defn- set-completed [val todos]
  (into {} (map (fn [[k v]] [k (assoc v :item/complete val)]) todos)))

(defmutation todo-check-all [ignored]
  (action [{:keys [state]}] (swap! state update :todo/by-id (partial set-completed true)))
  (remote [{:keys [ast state]}] (assoc ast :params {:id (:list @state)})))

(defmutation todo-uncheck-all [ignored]
  (action [{:keys [state]}] (swap! state update :todo/by-id (partial set-completed false)))
  (remote [{:keys [ast state]}] (assoc ast :params {:id (:list @state)})))

(defmutation todo-clear-complete [{:keys [list-id]}]
  (action [{:keys [state]}]
    (let [is-complete? (fn [item-ident] (get-in @state [:todo/by-id (second item-ident) :item/complete]))]
      (swap! state update-in [:list/by-id list-id :list/items]
        (fn [todos] (vec (remove (fn [ident] (is-complete? ident)) todos))))))
  (remote [{:keys [ast state]}] (assoc ast :params {:id (:list @state)})))

(defmutation todo-filter [{:keys [filter]}]
  (action [{:keys [state]}]
    (let [list-id (get-in @state [:todos 1])]
      (swap! state assoc-in [:list/by-id list-id :list/filter] filter))))
