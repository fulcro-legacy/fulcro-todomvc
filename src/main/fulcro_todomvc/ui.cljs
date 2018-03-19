(ns fulcro-todomvc.ui
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [fulcro.client.mutations :as mut :refer [defmutation]]
            [fulcro-todomvc.api :as api]
            [fulcro.i18n :refer [tr trf]]
            yahoo.intl-messageformat-with-locales
            [fulcro.client.dom :as old-dom]
            [fulcro.client.alpha.dom :as dom]
            [fulcro.client :as fc]))

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

    (dom/li {:className (cond-> ""
                          complete (str "completed")
                          editing (str " editing"))}
      (dom/div :.view
        (dom/input {:type      "checkbox"
                    :className "toggle"
                    :checked   (boolean complete)
                    :onChange  #(if complete (uncheck id) (check id))})
        (dom/label {:onDoubleClick (fn []
                                     (mut/toggle! this :ui/editing)
                                     (mut/set-string! this :ui/edit-text :value label))} label)
        (dom/button :.destroy {:onClick #(delete-item id)}))
      (dom/input {:ref       "edit_field"
                  :className "edit"
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
    (dom/header :.header
      (dom/h1 title)
      (dom/input {:value       (or new-item-text "")
                  :className   "new-todo"
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

    (dom/footer :.footer
      (dom/span :.todo-count
        (dom/strong (trf "{num,plural,=0 {no items} =1 {1 item} other {# items}} left" :num num-remaining)))
      (dom/ul :.filters
        (dom/li
          (dom/a {:className (when (or (nil? filter) (= :list.filter/none filter)) "selected")
                  :href      "#"} (tr "All")))
        (dom/li nil
          (dom/a {:className (when (= :list.filter/active filter) "selected")
                  :href      "#/active"} (tr "Active")))
        (dom/li nil
          (dom/a {:className (when (= :list.filter/completed filter) "selected")
                  :href      "#/completed"} (tr "Completed"))))
      (when (pos? num-completed)
        (dom/button {:className "clear-completed"
                     :onClick   #(prim/transact! component `[(api/todo-clear-complete {:list-id ~id})])} (tr "Clear Completed"))))))

(defn footer-info []
  (dom/footer :.info
    (dom/p (tr "Double-click to edit a todo"))
    (dom/p (tr "Created by ")
      (dom/a {:href   "http://www.fulcrologic.com"
              :target "_blank"} "Fulcrologic, LLC"))
    (dom/p "Part of "
      (dom/a {:href   "http://todomvc.com"
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

    (dom/div
      (dom/section :.todoapp
        (header this title)
        (when (pos? num-todos)
          (dom/div
            (dom/section :.main
              (dom/input {:type      "checkbox"
                          :className "toggle-all"
                          :checked   all-completed?
                          :onClick   (fn [] (if all-completed?
                                              (prim/transact! this `[(api/todo-uncheck-all {:list-id ~id})])
                                              (prim/transact! this `[(api/todo-check-all {:list-id ~id})])))})
              (dom/label {:htmlFor "toggle-all"} (tr "Mark all as complete"))
              (dom/ul :.todo-list
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
      (js/console.log :h (:fulcro.history/history-steps history)
        :c (count (:fulcro.history/history-steps history)))
      (assoc ast :params params))))

(defsc Application [this {:keys [ui/support-visible todos ui/locale ui/comment]}]
  {:initial-state (fn [p] {:todos              (prim/get-initial-state TodoList {})
                           :ui/support-visible false
                           :ui/comment         ""})
   :ident         (fn [] [:application :root])
   :query         [:ui/support-visible :ui/comment [:ui/locale '_] {:todos (prim/get-query TodoList)}]}
  (dom/div
    (dom/div :.locale-selector
      (old-dom/select #js {:value    (or locale "")
                           :onChange (fn [evt]
                                       (prim/transact! this `[(mut/change-locale {:lang ~(.. evt -target -value)})]))}
        (old-dom/option #js {:value "en-US"} "English")
        (old-dom/option #js {:value "es-MX"} "Español")))
    (dom/div :.support-request
      (if support-visible
        (dom/div
          (dom/textarea {:value    comment
                         :onChange (fn [evt] (mut/set-string! this :ui/comment :event evt))})
          (dom/br)
          (dom/button {:onClick (fn []
                                  (prim/transact! this `[(fulcro.client.mutations/send-history {:comment ~comment})])
                                  (mut/toggle! this :ui/support-visible)
                                  (mut/set-string! this :ui/comment :value ""))}
            (tr "Send Request")))
        (dom/button {:onClick #(mut/toggle! this :ui/support-visible)} (tr "Help!"))))
    (ui-todo-list todos)))

(def ui-application (prim/factory Application))

(defsc Root [this {:keys [root/application]}]
  {:initial-state (fn [p] {:ui/locale        "en-US"
                           :root/application (prim/get-initial-state Application {})})
   :query         [{:root/application (prim/get-query Application)}]}
  (dom/div
    (ui-application application)))
