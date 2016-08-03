(ns untangled-todomvc.ui
  (:require [om.next :as om :refer-macros [defui]]
            [untangled.client.mutations :as mut]
            [untangled.i18n :refer-macros [tr trf]]
            yahoo.intl-messageformat-with-locales
            [om.dom :as dom]))

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
      (let [input-field (js/ReactDOM.findDOMNode (.. this -refs -edit_field))
            input-field-length (.. input-field -value -length)]
        (.focus input-field)
        (.setSelectionRange input-field input-field-length input-field-length))))

  (render [this]
    (let [{:keys [db/id item/label item/complete ui/editing]} (om/props this)
          edit-text (om/get-state this :edit-text)
          {:keys [delete-item check uncheck]} (om/get-computed this)
          submit-edit (fn [evt]
                        (if-let [trimmed-text (trim-text (.. evt -target -value))]
                          (do
                            (om/transact! this `[(todo/edit ~{:id id :text trimmed-text})])
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
  static om/IQuery
  (query [this] [:db/id
                 {:list/items (om/get-query TodoItem)}
                 :list/title
                 :list/filter])
  Object
  (render [this]
    (let [{:keys [list/items list/filter list/title db/id]} (om/props this)
          num-todos (count items)
          completed-todos (filterv :item/complete items)
          num-completed (count completed-todos)
          all-completed? (= num-completed num-todos)
          filtered-todos (case filter
                           :list.filter/active (filterv (comp not :item/complete) items)
                           :list.filter/completed completed-todos
                           items)
          delete-item (fn [item-id] (om/transact! this `[(todo/delete-item ~{:id item-id})]))
          check (fn [item-id] (om/transact! this `[(todo/check ~{:id item-id})]))
          uncheck (fn [item-id] (om/transact! this `[(todo/uncheck ~{:id item-id})]))]

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
                                                                                        (om/transact! this `[(todo/uncheck-all)])
                                                                                        (om/transact! this `[(todo/check-all)])))
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
    (letfn [(add-item [evt]
              (when (is-enter? evt)
                (when-let [trimmed-text (trim-text (.. evt -target -value))]
                  (om/transact! this `[(todo/new-item ~{:id (om/tempid) :text trimmed-text})])
                  (set! (.. evt -target -value) ""))))]

      (dom/header #js {:className "header"}

                  (dom/h1 nil title)
                  (dom/input #js {:className   "new-todo"
                                  :placeholder (tr "What needs to be done?")
                                  :autoFocus   true
                                  :onKeyDown   add-item}))))

  (filter-footer [this num-todos num-completed]
    (let [{:keys [list/filter]} (om/props this)
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
                                     :onClick   #(om/transact! this `[(todo/clear-complete)])} (tr "Clear Completed"))))))

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
                                                     (om/transact! this `[(ui/change-locale {:lang ~(.. evt -target -value)})]))}
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
                                                               (om/transact! this `[(support-viewer/send-support-request {:comment ~comment}) (support/toggle)])
                                                               (om/update-state! this assoc :comment "")
                                                               )} (tr "Send Request")))
                          (dom/button #js {:onClick #(om/transact! this '[(support/toggle)])} (tr "Help!"))))

               (ui-todo-list todos)))))
