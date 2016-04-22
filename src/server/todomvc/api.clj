(ns todomvc.api
  (:require [om.next.server :as om]
            [datomic.api :as d]
            [untangled.datomic.protocols :as db]
            [taoensso.timbre :as timbre]))

(defmulti apimutate om/dispatch)

(defmethod apimutate :default [e k p]
  (timbre/error "Unrecognized mutation: " k p))

(defonce last-id (atom 1000))
(defonce requests (atom {}))

(defmethod apimutate 'support-viewer/send-support-request [e k p]
  {:action
   (fn []
     (let [_ (swap! last-id inc)
           id @last-id]
       (timbre/info "New support request " id)
       (swap! requests assoc id p)
       id))})

(defn resolve-ids [new-db omids->tempids tempids->realids]
  (reduce
    (fn [acc [cid dtmpid]]
      (assoc acc cid (d/resolve-tempid new-db tempids->realids dtmpid)))
    {}
    omids->tempids))

(defn make-list [connection list-name]
  (let [id (d/tempid :db.part/user)
        tx [{:db/id id :list/title list-name}]
        idmap (:tempids @(d/transact connection tx))
        real-id (d/resolve-tempid (d/db connection) idmap id)]
    real-id))

(defn find-list
  "Find or create a list with the given name. Always returns a valid list ID."
  [conn list-name]
  (if-let [eid (d/q '[:find ?e . :in $ ?n :where [?e :list/title ?n]] (d/db conn) list-name)]
    eid
    (make-list conn list-name)))

(defmethod apimutate 'todo/new-item [{:keys [todo-database]} _ {:keys [id text list]}]
  {:action #(let [connection (db/get-connection todo-database)
                  datomic-id (d/tempid :db.part/user)
                  omid->tempid {id datomic-id}
                  list-id (find-list connection list)
                  tx [[:db/add list-id :list/items datomic-id] {:db/id datomic-id :item/complete false :item/label text}]
                  result @(d/transact connection tx)
                  tempid->realid (:tempids result)
                  omids->realids (resolve-ids (d/db connection) omid->tempid tempid->realid)]
             {:tempids omids->realids})})

(defmethod apimutate 'todo/check [{:keys [todo-database]} _ {:keys [id]}]
  {:action #(let [connection (db/get-connection todo-database)
                  tx [[:db/add id :item/complete true]]]
             @(d/transact connection tx)
             true)})

(defmethod apimutate 'todo/uncheck [{:keys [todo-database]} _ {:keys [id]}]
  {:action #(let [connection (db/get-connection todo-database)
                  tx [[:db/add id :item/complete false]]]
             @(d/transact connection tx)
             true)})

(defmethod apimutate 'todo/edit [{:keys [todo-database]} _ {:keys [id text]}]
  {:action #(let [connection (db/get-connection todo-database)
                  tx [[:db/add id :item/label text]]]
             @(d/transact connection tx)
             true)})

(defn- set-checked [connection list-name value]
  (let [ids (d/q '[:find [?e ...] :in $ ?list-name
                   :where
                   [?list-id :list/title ?list-name]
                   [?list-id :list/items ?e]] (d/db connection) list-name)
        tx (mapv (fn [id] [:db/add id :item/complete value]) ids)]
    @(d/transact connection tx)
    true))

(defmethod apimutate 'todo/check-all [{:keys [todo-database]} _ {:keys [id]}]
  {:action #(let [connection (db/get-connection todo-database)] (set-checked connection id true))})

(defmethod apimutate 'todo/uncheck-all [{:keys [todo-database]} _ {:keys [id]}]
  {:action #(let [connection (db/get-connection todo-database)] (set-checked connection id false))})

(defmethod apimutate 'todo/delete-item [{:keys [todo-database]} _ {:keys [id]}]
  {:action #(let [connection (db/get-connection todo-database)
                  tx [[:db.fn/retractEntity id]]]
             @(d/transact connection tx)
             true)})

(defmethod apimutate 'todo/clear-complete [{:keys [todo-database]} _ {:keys [id]}]
  {:action #(let [connection (db/get-connection todo-database)
                  ids (d/q '[:find [?e ...] :in $ ?list-name
                             :where
                             [?list-id :list/title ?list-name]
                             [?list-id :list/items ?e]
                             [?e :item/complete true]] (d/db connection) id)
                  tx (mapv (fn [id] [:db.fn/retractEntity id]) ids)]
             @(d/transact connection tx)
             true)})

(defn ensure-integer [n]
  (cond
    (string? n) (Integer/parseInt n)
    :else n))

(defn read-list [connection query nm]
  (let [list-id (find-list connection nm)
        db (d/db connection)
        rv (d/pull db query list-id)]
    rv))

(defn api-read [{:keys [todo-database query] :as env} k {:keys [list] :as params}]
  (let [connection (db/get-connection todo-database)]
    (timbre/info "Query: " query)
    (case k
      :todos {:value (read-list connection query list)}
      :support-request (let [id (:id params)]
                         (timbre/info "Sending history for " id)
                         {:value (get @requests (ensure-integer id))})
      (timbre/error "Unrecognized read: " k))))
