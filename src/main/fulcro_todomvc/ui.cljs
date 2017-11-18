(ns fulcro-todomvc.ui
  (:require [fulcro.client.primitives :as om :refer [defui]]
            [fulcro.client.mutations :as mut]
            [fulcro-todomvc.api :as api]
            [fulcro.i18n :refer [tr trf]]
            yahoo.intl-messageformat-with-locales
            [fulcro.client.dom :as dom]
            [fulcro.client.core :as uc]))

(defn is-enter? [evt] (= 13 (.-keyCode evt)))
(defn is-escape? [evt] (= 27 (.-keyCode evt)))

(defn trim-text [text]
  "Returns text without surrounding whitespace if not empty, otherwise nil"
  (let [trimmed-text (clojure.string/trim text)]
    (when-not (empty? trimmed-text)
      trimmed-text)))

(defui ^:once TodoItem
  static om/IQuery
  (query [_] [:db/id :item/label :item/complete :ui/editing])
  static om/Ident
  (ident [_ props] [:todo/by-id (:db/id props)])
  Object
  (initLocalState [this]
    {:edit-text (or (:text (om/props this)) "")})

  (componentDidUpdate [this prev-props _]
    ;; Code adapted from React TodoMVC implementation
    (when (and (not (:editing prev-props)) (:editing (om/props this)))
      (let [input-field        (js/ReactDOM.findDOMNode (.. this -refs -edit_field))
            input-field-length (.. input-field -value -length)]
        (.focus input-field)
        (.setSelectionRange input-field input-field-length input-field-length))))

  (render [this]
    (let [{:keys [db/id item/label item/complete ui/editing] :or {complete false}} (om/props this)
          edit-text   (om/get-state this :edit-text)
          {:keys [delete-item check uncheck]} (om/get-computed this)
          submit-edit (fn [evt]
                        (if-let [trimmed-text (trim-text (.. evt -target -value))]
                          (do
                            (om/transact! this `[(api/todo-edit ~{:id id :text trimmed-text})])
                            (om/update-state! this assoc :edit-text trimmed-text)
                            (mut/toggle! this :ui/editing))
                          (delete-item id)))]

      (dom/li #js {:className (cond-> ""
                                complete (str "completed")
                                editing (str " editing"))}
        (dom/div #js {:className "view"}
          (dom/input #js {:className "toggle"
                          :type      "checkbox"
                          :checked   complete
                          :onChange  #(if complete (uncheck id) (check id))})
          (dom/label #js {:onDoubleClick (fn []
                                           (mut/toggle! this :ui/editing)
                                           (om/update-state! this assoc :edit-text label))} label)
          (dom/button #js {:className "destroy"
                           :onClick   #(delete-item id)}))
        (dom/input #js {:className "edit"
                        :ref       "edit_field"
                        :value     edit-text
                        :onChange  #(om/update-state! this assoc :edit-text (.. % -target -value))
                        :onKeyDown #(cond
                                      (is-enter? %) (submit-edit %)
                                      (is-escape? %) (do (om/update-state! this assoc :edit-text label)
                                                         (mut/toggle! this :ui/editing)))
                        :onBlur    #(when editing (submit-edit %))})))))

(def ui-todo-item (om/factory TodoItem {:keyfn :db/id}))

(defui ^:once TodoList
  static uc/InitialAppState
  (initial-state [t p] {:db/id (om/tempid) :ui/new-item-text "" :list/items [] :list/title "main" :list/filter :list.filter/none})
  static om/Ident
  (ident [this props] [:list/by-id (:db/id props)])
  static om/IQuery
  (query [this] [:db/id
                 :ui/new-item-text
                 {:list/items (om/get-query TodoItem)}
                 :list/title
                 :list/filter])
  Object
  (render [this]
    (let [{:keys [list/items list/filter list/title db/id]} (om/props this)
          num-todos       (count items)
          completed-todos (filterv :item/complete items)
          num-completed   (count completed-todos)
          all-completed?  (= num-completed num-todos)
          filtered-todos  (case filter
                            :list.filter/active (filterv (comp not :item/complete) items)
                            :list.filter/completed completed-todos
                            items)
          delete-item     (fn [item-id] (om/transact! this `[(api/todo-delete-item ~{:list-id id :id item-id})]))
          check           (fn [item-id] (om/transact! this `[(api/todo-check ~{:id item-id})]))
          uncheck         (fn [item-id] (om/transact! this `[(api/todo-uncheck ~{:id item-id})]))]

      (dom/div nil

        (dom/section #js {:className "todoapp"}

          (.header this title)

          (when (pos? num-todos)
            (dom/div nil
              (dom/section #js {:className "main"}
                (dom/input #js {:className "toggle-all"
                                :type      "checkbox"
                                :checked   all-completed?
                                :onClick   (fn [] (if all-completed?
                                                    (om/transact! this `[(api/todo-uncheck-all {})])
                                                    (om/transact! this `[(api/todo-check-all {})])))
                                })
                (dom/label #js {:htmlFor "toggle-all"} (tr "Mark all as complete"))
                (dom/ul #js {:className "todo-list"}
                  (map #(ui-todo-item (om/computed %
                                        {:delete-item delete-item
                                         :check       check
                                         :uncheck     uncheck})) filtered-todos)))

              (.filter-footer this num-todos num-completed))))

        (.footer-info this))))

  (header [this title]
    (let [{:keys [db/id ui/new-item-text]} (om/props this)]
      (dom/header #js {:className "header"}
        (dom/h1 nil title)
        (dom/input #js {:className   "new-todo"
                        :value       (or new-item-text "")
                        :onKeyDown   (fn [evt]
                                       (when (is-enter? evt)
                                         (when-let [trimmed-text (trim-text (.. evt -target -value))]
                                           (om/transact! this `[(api/todo-new-item ~{:list-id id
                                                                                     :id      (om/tempid)
                                                                                     :text    trimmed-text})]))))
                        :onChange    (fn [evt] (mut/set-string! this :ui/new-item-text :event evt))
                        :placeholder (tr "What needs to be done?")
                        :autoFocus   true}))))

  (filter-footer [this num-todos num-completed]
    (let [{:keys [db/id list/filter]} (om/props this)
          num-remaining (- num-todos num-completed)]

      (dom/footer #js {:className "footer"}
        (dom/span #js {:className "todo-count"}
          (dom/strong nil (trf "{num,plural,=0 {no items} =1 {1 item} other {# items}} left" :num num-remaining)))
        (dom/ul #js {:className "filters"}
          (dom/li nil
            (dom/a #js {:className (when (or (nil? filter) (= :list.filter/none filter)) "selected")
                        :href      "#"} (tr "All")))
          (dom/li nil
            (dom/a #js {:className (when (= :list.filter/active filter) "selected")
                        :href      "#/active"} (tr "Active")))
          (dom/li nil
            (dom/a #js {:className (when (= :list.filter/completed filter) "selected")
                        :href      "#/completed"} (tr "Completed"))))
        (when (pos? num-completed)
          (dom/button #js {:className "clear-completed"
                           :onClick   #(om/transact! this `[(api/todo-clear-complete {:list-id ~id})])} (tr "Clear Completed"))))))

  (footer-info [this]
    (dom/footer #js {:className "info"}
      (dom/p nil (tr "Double-click to edit a todo"))
      (dom/p nil (tr "Created by ")
        (dom/a #js {:href   "http://www.thenavisway.com"
                    :target "_blank"} "NAVIS"))
      (dom/p nil "Part of "
        (dom/a #js {:href   "http://todomvc.com"
                    :target "_blank"} "TodoMVC")))))

(def ui-todo-list (om/factory TodoList))

(defui ^:once Root
  static uc/InitialAppState
  (initial-state [c p] {:todos (uc/get-initial-state TodoList {})})
  static om/IQuery
  (query [this] `[:ui/support-visible :ui/react-key :ui/locale {:todos ~(om/get-query TodoList)}])
  Object
  (initLocalState [this] {:comment ""})
  (render [this]
    (let [{:keys [ui/support-visible ui/react-key todos ui/locale] :or {ui/react-key "ROOT"}} (om/props this)
          comment (om/get-state this :comment)]
      (dom/div #js {:key (or react-key "ROOT")}
        (dom/div #js {:className "locale-selector"}
          (dom/select #js {:value    locale
                           :onChange (fn [evt]
                                       (om/transact! this `[(mut/change-locale {:lang ~(.. evt -target -value)})]))}
            (dom/option #js {:value "en-US"} "English")
            (dom/option #js {:value "es-MX"} "Español")))
        (dom/div #js {:className "support-request"}
          (if support-visible
            (dom/div #js {}
              (dom/textarea #js {:value    comment
                                 :onChange (fn [evt]
                                             (om/update-state! this assoc :comment (.. evt -target -value)))})
              (dom/br nil)
              (dom/button #js {:onClick (fn []
                                          (om/transact! this `[(mut/send-history {:comment ~comment}) (api/toggle-support {})])
                                          (om/update-state! this assoc :comment "")
                                          )} (tr "Send Request")))
            (dom/button #js {:onClick #(om/transact! this `[(api/toggle-support {})])} (tr "Help!"))))

        (ui-todo-list todos)))))
