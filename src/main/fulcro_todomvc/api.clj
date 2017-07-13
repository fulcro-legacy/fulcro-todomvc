(ns fulcro-todomvc.api
  (:require [om.next.server :as om]
            [datomic.api :as d]
            [fulcro.datomic.protocols :as db]
            [fulcro.server :refer [defmutation defquery-root]]
            [taoensso.timbre :as timbre]))

(defonce last-id (atom 1000))
(defonce requests (atom {}))

; One way to make a mutation with an explicity FQ symbol. The return value of this function must a map
; with a lambda at the `:action` key.
(defmethod fulcro.server/server-mutate 'support/send-request [e k p]
  {:action
   (fn []
     (let [_  (swap! last-id inc)
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

; When using the untangled-parser on the server, you may mimic the client-side defmutation as long as your
; server-side namespace for the mutation is the same name as the client (since it puts the mutation in the given namespace)
(defmutation todo-new-item [{:keys [id text list]}]
  (action [{:keys [todo-database]}]
    (let [connection     (db/get-connection todo-database)
          datomic-id     (d/tempid :db.part/user)           ; in order to create an entity, we need a proper datomic temp ID
          omid->tempid   {id datomic-id}                    ; remember that the incoming temp id (id) maps to the datomic one
          list-id        (find-list connection list)        ; find the list
          tx             [[:db/add list-id :list/items datomic-id] {:db/id datomic-id :item/complete false :item/label text}]
          result         @(d/transact connection tx)
          tempid->realid (:tempids result)                  ; remap the incoming om tempid to the now-real datomic ID
          omids->realids (resolve-ids (d/db connection) omid->tempid tempid->realid)]
      (timbre/info "Added list item " text " to " list)
      {:tempids omids->realids})))

(defmutation todo-check [{:keys [id]}]
  (action [{:keys [todo-database]}]
    (let [connection (db/get-connection todo-database)
          tx         [[:db/add id :item/complete true]]]
      @(d/transact connection tx)
      (timbre/info "Checked list item " id)
      true)))

(defmutation todo-uncheck [{:keys [id]}]
  (action [{:keys [todo-database]}]
    (let [connection (db/get-connection todo-database)
          tx         [[:db/add id :item/complete false]]]
      @(d/transact connection tx)
      (timbre/info "Unchecked list item " id)
      true)))

(defmutation todo-edit [{:keys [id text]}]
  (action [{:keys [todo-database]}]
    (let [connection (db/get-connection todo-database)
          tx         [[:db/add id :item/label text]]]
      @(d/transact connection tx)
      (timbre/info "Updated list item " id " to " text)
      true)))

(defn- set-checked [connection list-name value]
  (let [ids (d/q '[:find [?e ...] :in $ ?list-name
                   :where
                   [?list-id :list/title ?list-name]
                   [?list-id :list/items ?e]] (d/db connection) list-name)
        tx  (mapv (fn [id] [:db/add id :item/complete value]) ids)]
    @(d/transact connection tx)
    (timbre/info "Set all items in " list-name " to " (if value "checked" "unchecked"))
    true))

(defmutation todo-check-all [{:keys [id]}]
  (action [{:keys [todo-database]}]
    (let [connection (db/get-connection todo-database)] (set-checked connection id true))))

(defmutation todo-uncheck-all [{:keys [id]}]
  (action [{:keys [todo-database]}]
    (let [connection (db/get-connection todo-database)] (set-checked connection id false))))

(defmutation todo-delete-item [{:keys [id]}]
  (action [{:keys [todo-database]}]
    (let [connection (db/get-connection todo-database)
          tx         [[:db.fn/retractEntity id]]]
      @(d/transact connection tx)
      (timbre/info "Deleted item " id)
      true)))

(defmutation todo-clear-complete [{:keys [id]}]
  (action [{:keys [todo-database]}]
    (let [connection (db/get-connection todo-database)
          ids        (d/q '[:find [?e ...] :in $ ?list-name
                            :where
                            [?list-id :list/title ?list-name]
                            [?list-id :list/items ?e]
                            [?e :item/complete true]] (d/db connection) id)
          tx         (mapv (fn [id] [:db.fn/retractEntity id]) ids)]
      @(d/transact connection tx)
      (timbre/info "Deleted all cleared items in list " id)
      true)))

(defn ensure-integer [n]
  (cond
    (string? n) (Integer/parseInt n)
    :else n))

(defn read-list [connection query nm]
  (let [list-id (find-list connection nm)
        db      (d/db connection)
        rv      (d/pull db query list-id)]
    rv))

(defquery-root :todos
  "Returns the todo items for the given list."
  (value [{:keys [query todo-database]} {:keys [list]}]
    (timbre/info "Responding to request for list: " list)
    (let [connection (db/get-connection todo-database)]
      (read-list connection query list))))

(defquery-root :support-request
  "Get a support request by server ID (see server logs (NOT CLIENT Tx ID)"
  (value [env {:keys [id]}]
    (let [id      (ensure-integer id)
          history (get @requests id [])]
      (timbre/info "Request for client history: " id)
      (when-not (seq history)
        (timbre/error "Invalid history ID! Perhaps you used a client tx id instead? Known IDs are: " (pr-str (keys @requests))))
      history)))
