(ns fulcro-todomvc.ui
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [fulcro.client.mutations :as mut :refer [defmutation]]
            [fulcro-todomvc.api :as api]
            [fulcro.i18n :refer [tr trf]]
            yahoo.intl-messageformat-with-locales
            [fulcro.client.alpha.dom :as d]))

(defn is-enter? [evt] (= 13 (.-keyCode evt)))
(defn is-escape? [evt] (= 27 (.-keyCode evt)))

(defn trim-text [text]
  "Returns text without surrounding whitespace if not empty, otherwise nil"
  (let [trimmed-text (clojure.string/trim text)]
    (when-not (empty? trimmed-text)
      trimmed-text)))

(defsc TodoItem [this
                 {:keys [db/id item/label item/complete ui/editing ui/edit-text] :or {complete false} :as props}
                 {:keys [delete-item check uncheck] :as computed}]
  {:query              [:db/id :item/label :item/complete :ui/editing :ui/edit-text]
   :ident              [:todo/by-id :db/id]
   :componentDidUpdate (fn [prev-props _]
                         ;; Code adapted from React TodoMVC implementation
                         (when (and (not (:editing prev-props)) (:editing (prim/props this)))
                           (let [input-field        (js/ReactDOM.findDOMNode (.. this -refs -edit_field))
                                 input-field-length (.. input-field -value -length)]
                             (.focus input-field)
                             (.setSelectionRange input-field input-field-length input-field-length))))}
  (let [submit-edit (fn [evt]
                      (if-let [trimmed-text (trim-text (.. evt -target -value))]
                        (do
                          (prim/transact! this `[(api/commit-label-change ~{:id id :text trimmed-text})])
                          (mut/set-string! this :ui/edit-text :value trimmed-text)
                          (mut/toggle! this :ui/editing))
                        (delete-item id)))]

    (d/li {:className (cond-> ""
                      complete (str "completed")
                      editing (str " editing"))}
      (d/div :.view
        (d/input :.toggle {:type     "checkbox"
                         :checked  (boolean complete)
                         :onChange #(if complete (uncheck id) (check id))})
        (d/label {:onDoubleClick (fn []
                                   (mut/toggle! this :ui/editing)
                                   (mut/set-string! this :ui/edit-text :value label))} label)
        (d/button :.destroy {:onClick #(delete-item id)}))
      (d/input :.edit {:ref       "edit_field"
                     :value     (or edit-text "")
                     :onChange  #(mut/set-string! this :ui/edit-text :event %)
                     :onKeyDown #(cond
                                   (is-enter? %) (submit-edit %)
                                   (is-escape? %) (do (mut/set-string! this :ui/edit-text :value label)
                                                      (mut/toggle! this :ui/editing)))
                     :onBlur    #(when editing (submit-edit %))}))))

(def ui-todo-item (prim/factory TodoItem {:keyfn :db/id}))


(defn header [component title]
  (let [{:keys [db/id ui/new-item-text]} (prim/props component)]
    (d/header :.header
      (d/h1 title)
      (d/input :.new-todo {:value       (or new-item-text "")
                         :onKeyDown   (fn [evt]
                                        (when (is-enter? evt)
                                          (when-let [trimmed-text (trim-text (.. evt -target -value))]
                                            (prim/transact! component `[(api/todo-new-item ~{:list-id id
                                                                                             :id      (prim/tempid)
                                                                                             :text    trimmed-text})]))))
                         :onChange    (fn [evt] (mut/set-string! component :ui/new-item-text :event evt))
                         :placeholder (tr "What needs to be done?")
                         :autoFocus   true}))))

(defn filter-footer [component num-todos num-completed]
  (let [{:keys [db/id list/filter]} (prim/props component)
        num-remaining (- num-todos num-completed)]

    (d/footer :.footer
      (d/span :.todo-count
        (d/strong (trf "{num,plural,=0 {no items} =1 {1 item} other {# items}} left" :num num-remaining)))
      (d/ul :.filters
        (d/li (d/a {:className (when (or (nil? filter) (= :list.filter/none filter)) "selected")
                :href      "#"} (tr "All")))
        (d/li (d/a {:className (when (= :list.filter/active filter) "selected")
                :href      "#/active"} (tr "Active")))
        (d/li (d/a {:className (when (= :list.filter/completed filter) "selected")
                :href      "#/completed"} (tr "Completed"))))
      (when (pos? num-completed)
        (d/button :.clear-completed {:onClick #(prim/transact! component `[(api/todo-clear-complete {:list-id ~id})])} (tr "Clear Completed"))))))

(defn footer-info []
  (d/footer :.info
    (d/p (tr "Double-click to edit a todo"))
    (d/p (tr "Created by ")
      (d/a {:href   "http://www.fulcrologic.com"
          :target "_blank"} "Fulcrologic, LLC"))
    (d/p "Part of "
      (d/a {:href   "http://todomvc.com"
          :target "_blank"} "TodoMVC"))))

(defsc TodoList [this {:keys [list/items list/filter list/title db/id]}]
  {:initial-state (fn [p] {:db/id (prim/tempid) :ui/new-item-text "" :list/items [] :list/title "main" :list/filter :list.filter/none})
   :ident         [:list/by-id :db/id]
   :query         [:db/id :ui/new-item-text {:list/items (prim/get-query TodoItem)} :list/title :list/filter]}
  (let [num-todos       (count items)
        completed-todos (filterv :item/complete items)
        num-completed   (count completed-todos)
        all-completed?  (= num-completed num-todos)
        filtered-todos  (case filter
                          :list.filter/active (filterv (comp not :item/complete) items)
                          :list.filter/completed completed-todos
                          items)
        delete-item     (fn [item-id] (prim/transact! this `[(api/todo-delete-item ~{:list-id id :id item-id})]))
        check           (fn [item-id] (prim/transact! this `[(api/todo-check ~{:id item-id})]))
        uncheck         (fn [item-id] (prim/transact! this `[(api/todo-uncheck ~{:id item-id})]))]

    (d/div
      (d/section :.todoapp
        (header this title)
        (when (pos? num-todos)
          (d/div
            (d/section :.main
              (d/input :.toggle-all {:type    "checkbox"
                                   :checked all-completed?
                                   :onClick (fn [] (if all-completed?
                                                     (prim/transact! this `[(api/todo-uncheck-all {:list-id ~id})])
                                                     (prim/transact! this `[(api/todo-check-all {:list-id ~id})])))})
              (d/label {:htmlFor "toggle-all"} (tr "Mark all as complete"))
              (d/ul :.todo-list
                (map #(ui-todo-item (prim/computed %
                                      {:delete-item delete-item
                                       :check       check
                                       :uncheck     uncheck})) filtered-todos)))
            (filter-footer this num-todos num-completed))))
      (footer-info))))

(def ui-todo-list (prim/factory TodoList))

(defmutation fulcro.client.mutations/send-history
  "Send the current app history to the server. The params can include anything and will be merged with a `:history` entry.
  Your server implementation of `fulcro.client.mutations/send-history` should record the data of history for
  retrieval by a root query for :support-request, which should at least include the stored :history and optionally a
  :comment from the user. You should add whatever identity makes sense for tracking."
  [params]
  (remote [{:keys [reconciler state ast]}]
    (let [history (-> reconciler (prim/get-history) deref)
          params  (assoc params :history history)]
      (assoc ast :params params))))

(defsc Application [this {:keys [ui/support-visible ui/react-key todos ui/locale ui/comment] :or {ui/react-key "ROOT"}}]
  {:initial-state (fn [p] {:todos              (prim/get-initial-state TodoList {})
                           :ui/support-visible false
                           :ui/comment         ""})
   :ident         (fn [] [:application :root])
   :query         [:ui/support-visible :ui/comment :ui/react-key [:ui/locale '_] {:todos (prim/get-query TodoList)}]}
  (d/div
    (d/div :.locale-selector
      (d/select {:value    (or locale "")
               :onChange (fn [evt]
                           (prim/transact! this `[(mut/change-locale {:lang ~(.. evt -target -value)})]))}
        (d/option {:value "en-US"} "English")
        (d/option {:value "es-MX"} "Español")))
    (d/div :.support-request
      (if support-visible
        (d/div
          (d/textarea {:value    comment
                     :onChange (fn [evt] (mut/set-string! this :ui/comment :event evt))})
          (d/br)
          (d/button {:onClick (fn []
                              (prim/transact! this `[(fulcro.client.mutations/send-history {:comment ~comment})])
                              (mut/toggle! this :ui/support-visible)
                              (mut/set-string! this :ui/comment :value ""))}
            (tr "Send Request")))
        (d/button {:onClick #(mut/toggle! this :ui/support-visible)} (tr "Help!"))))
    (ui-todo-list todos)))

(def ui-application (prim/factory Application))

(defsc Root [this {:keys [root/application]}]
  {:initial-state (fn [p] {:ui/locale        "en-US"
                           :root/application (prim/get-initial-state Application {})})
   :query         [{:root/application (prim/get-query Application)}]}
  (d/div
    (ui-application application)))
