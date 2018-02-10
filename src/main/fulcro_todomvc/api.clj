(ns fulcro-todomvc.api
  (:require [fulcro.server :as om]
            [datomic.api :as d]
            [fulcro.datomic.protocols :as db]
            [fulcro.server :refer [defmutation defquery-root]]
            [fulcro.logging :as log]))

(defonce last-id (atom 1000))
(defonce requests (atom {}))

; You can use a fully-qualified symbol with defmutation, and it will honor it. You cannot intern it though.
; This is special. Support viewer defines the client side of this. We have to define how the server receives it.
(defmutation fulcro.client.mutations/send-history
  "Server reception of a support request with history. Persists in an in-memory db for this demo."
  [p]
  (action [env]
    (let [_  (swap! last-id inc)
          id @last-id]
      (log/info "New support request " id)
      (swap! requests assoc id p)
      id)))

(defn resolve-ids
  "Helper function to map from Fulcro tempids through Datomic tempids down to real IDs."
  [new-db fulcroids->tempids tempids->realids]
  (reduce
    (fn [acc [cid dtmpid]]
      (assoc acc cid (d/resolve-tempid new-db tempids->realids dtmpid)))
    {}
    fulcroids->tempids))

(defn make-list
  "Make a new list with the given title on the given Datomic database connection.

  Returns the real ID of the new list."
  [connection list-name]
  (let [id      (d/tempid :db.part/user)
        tx      [{:db/id id :list/title list-name}]
        idmap   (:tempids @(d/transact connection tx))
        real-id (d/resolve-tempid (d/db connection) idmap id)]
    real-id))

(defn find-list
  "Find or create a list with the given name. Always returns a valid list ID."
  [conn list-name]
  (if-let [eid (d/q '[:find ?e . :in $ ?n :where [?e :list/title ?n]] (d/db conn) list-name)]
    eid
    (make-list conn list-name)))

(defmutation todo-new-item
  [{:keys [id text list-id]}]
  (action [{:keys [todo-database]}]
    (let [connection         (db/get-connection todo-database) ; See fulcro-datomic for this API
          datomic-id         (d/tempid :db.part/user)       ; in order to create an entity, we need a proper datomic temp ID
          fulcroid->tempid   {id datomic-id}                ; remember that the incoming temp id (id) maps to the datomic one
          ; The Datomic list of new facts to add to the database.
          tx                 [[:db/add list-id :list/items datomic-id] {:db/id datomic-id :item/complete false :item/label text}]
          result             @(d/transact connection tx)
          tempid->realid     (:tempids result)              ; remap the incoming om tempid to the now-real datomic ID
          fulcroids->realids (resolve-ids (d/db connection) fulcroid->tempid tempid->realid)]
      (log/info "Added list item " text " to " list)
      {:tempids fulcroids->realids})))

(defmutation todo-check [{:keys [id]}]
  (action [{:keys [todo-database]}]
    (let [connection (db/get-connection todo-database)
          tx         [[:db/add id :item/complete true]]] ; New datomic fact. The entity at ID is not complete.
      @(d/transact connection tx)
      (log/info "Checked list item " id)
      true)))

(defmutation todo-uncheck [{:keys [id]}]
  (action [{:keys [todo-database]}]
    (let [connection (db/get-connection todo-database)
          tx         [[:db/add id :item/complete false]]]
      @(d/transact connection tx)
      (log/info "Unchecked list item " id)
      true)))

(defmutation commit-label-change [{:keys [id text]}]
  (action [{:keys [todo-database]}]
    (let [connection (db/get-connection todo-database)
          tx         [[:db/add id :item/label text]]]
      @(d/transact connection tx)
      (log/info "Updated list item " id " to " text)
      true)))

(defn- set-checked
  [connection list-id value]
  (let [ids (d/q '[:find [?e ...] :in $ ?list-id ; find all of the entity IDs that are the join target of the given list entity's items
                   :where
                   [?list-id :list/items ?e]] (d/db connection) list-id)
        tx  (mapv (fn [id] [:db/add id :item/complete value]) ids)] ; make a tx that updates the complete fact on the all.
    @(d/transact connection tx)
    (log/info "Set all items in " list-id " to " (if value "checked" "unchecked"))
    true))

(defmutation todo-check-all [{:keys [list-id]}]
  (action [{:keys [todo-database]}]
    (let [connection (db/get-connection todo-database)] (set-checked connection list-id true))))

(defmutation todo-uncheck-all [{:keys [list-id]}]
  (action [{:keys [todo-database]}]
    (let [connection (db/get-connection todo-database)] (set-checked connection list-id false))))

(defmutation todo-delete-item [{:keys [list-id id]}]
  (action [{:keys [todo-database]}]
    (let [connection (db/get-connection todo-database)
          tx         [[:db.fn/retractEntity id]]] ; the graph edges (:list/items) self-heal in Datomic.
      @(d/transact connection tx)
      (log/info "Deleted item " id)
      true)))

(defmutation todo-clear-complete [{:keys [list-id]}]
  (action [{:keys [todo-database]}]
    (let [connection (db/get-connection todo-database)
          ids        (d/q '[:find [?e ...] :in $ ?list-id ; find all entity IDs where they are items in list-id and complete = true
                            :where
                            [?list-id :list/items ?e]
                            [?e :item/complete true]] (d/db connection) list-id)
          tx         (mapv (fn [id] [:db.fn/retractEntity id]) ids)] ; make a tx that retracts them all (:list/items edges self-heal)
      @(d/transact connection tx)
      (log/info "Deleted all cleared items in list " list-id)
      true)))

(defn ensure-integer [n]
  (cond
    (string? n) (Integer/parseInt n)
    :else n))

(defn read-list [connection query nm]
  (let [list-id (find-list connection nm)
        db      (d/db connection)
        rv      (d/pull db query list-id)] ; Datomic's pull can handle Fulcro query syntax
    rv))

(defquery-root :todos
  "Returns the todo items for the given list."
  (value [{:keys [query todo-database]} {:keys [list]}]
    (log/info "Responding to request for list: " list)
    (let [connection (db/get-connection todo-database)]
      (read-list connection query list))))

(defquery-root :support-request
  "Get a support request by server ID (see server logs (NOT CLIENT Tx ID). This is required for the support viewer
  to work. You simply return the EDN that you saved earlier for the given support request."
  (value [env {:keys [id]}]
    (let [id      (ensure-integer id)
          history (get @requests id [])]
      (log/info "Request for client history: " id)
      (when-not (seq history)
        (log/error "Invalid history ID! Perhaps you used a client tx id instead? Known IDs are: " (pr-str (keys @requests))))
      history)))
