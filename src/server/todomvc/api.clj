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
  (let [_ (swap! last-id inc)
        id @last-id]
    {:action
     (fn []
       (timbre/info "New support request " id)
       (swap! requests assoc id p)
       id)}))

(defn resolve-ids [new-db omids->tempids tempids->realids]
  (reduce
    (fn [acc [cid dtmpid]]
      (assoc acc cid (d/resolve-tempid new-db tempids->realids dtmpid)))
    {}
    omids->tempids))

(defn replace-ref-types [dbc refs m]
  "@dbc   the database to query
   @refs  a set of keywords that ref datomic entities, which you want to access directly
          (rather than retrieving the entity id)
   @m     map returned from datomic pull containing the entity IDs you want to deref"
  (clojure.walk/postwalk
    (fn [arg]
      (if (and (coll? arg) (refs (first arg)))
        (update-in arg [1] (comp :db/ident (partial d/entity dbc) :db/id))
        arg))
    m))

(defn make-list [connection list-name]
  (let [id (d/tempid :db.part/user)
        tx [{:db/id id :list/title list-name :list/filter :list.filter/none}]
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
  (let [connection (db/get-connection todo-database)
        datomic-id (d/tempid :db.part/user)
        omid->tempid {id datomic-id}
        list-id (find-list connection list)
        tx [[:db/add list-id :list/items datomic-id] {:db/id datomic-id :item/complete false :item/label text}]
        result @(d/transact connection tx)
        tempid->realid (:tempids result)
        omids->realids (resolve-ids (d/db connection) omid->tempid tempid->realid)]
    {:tempids omids->realids}))

(defmethod apimutate 'todo/filter [{:keys [todo-database]} _ {:keys [filter list] :or {filter :list.filter/none list "main"}}]
  (let [connection (db/get-connection todo-database)
        list-id (find-list connection list)
        tx [[:db/add list-id :list/filter filter]]]
    @(d/transact connection tx)))

(defmethod apimutate 'todo/check [{:keys [todo-database]} _ {:keys [id]}]
  (let [connection (db/get-connection todo-database)
        tx [[:db/add id :item/complete true]]]
    @(d/transact connection tx)))

(defmethod apimutate 'todo/uncheck [{:keys [todo-database]} _ {:keys [id]}]
  (let [connection (db/get-connection todo-database)
        tx [[:db/add id :item/complete false]]]
    @(d/transact connection tx)))

(defn ensure-integer [n]
  (cond
    (string? n) (Integer/parseInt n)
    :else n))

(defn read-list [connection query nm]
  (let [list-id (find-list connection nm)
        db (d/db connection)
        rv (replace-ref-types db #{:list/filter} (d/pull db query list-id))]
    (timbre/info list-id query rv)
    rv))

(defn api-read [{:keys [todo-database query] :as env} k {:keys [list] :as params}]
  (let [connection (db/get-connection todo-database)]
    (case k
      :todos/filter :none
      :todos {:value (read-list connection query list)}
      :support-request (let [id (:id params)]
                         (timbre/info "Sending history for " id)
                         {:value (get @requests (ensure-integer id))})
      (timbre/error "Unrecognized read: " k))))
