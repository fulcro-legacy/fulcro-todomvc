(ns untangled-todomvc.ui
  (:require [om.next :as om :refer-macros [defui]]
            [untangled.client.mutations :as mut]
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
  (query [_] [:id :text :completed :editing])
  static om/Ident
  (ident [_ props] [:todo/by-id (:id props)])
  Object
  (initLocalState [this]
    {:edit-text (:text (om/props this))})

  (componentDidUpdate [this prev-props _]
    ;; Code adapted from React TodoMVC implementation
    (when (and (not (:editing prev-props)) (:editing (om/props this)))
      (let [input-field (js/ReactDOM.findDOMNode (.. this -refs -edit_field))
            input-field-length (.. input-field -value -length)]
        (.focus input-field)
        (.setSelectionRange input-field input-field-length input-field-length))))

  (render [this]
    (let [{:keys [id text completed editing]} (om/props this)
          edit-text (om/get-state this :edit-text)
          delete-item (om/get-computed this :onDelete)
          submit-edit (fn [evt]
                        (if-let [trimmed-text (trim-text (.. evt -target -value))]
                          (do
                            (om/transact! this `[(todo/edit ~{:id id :text trimmed-text})])
                            (om/update-state! this assoc :edit-text trimmed-text)
                            (mut/toggle! this :editing))
                          (delete-item id)))]

      (dom/li #js {:className (cond-> ""
                                completed (str "completed")
                                editing (str " editing"))}
        (dom/div #js {:className "view"}
          (dom/input #js {:className "toggle"
                          :type      "checkbox"
                          :checked   completed
                          :onChange  #(om/transact! this `[(todo/toggle-complete ~{:id id}) :todos/num-completed])})
          (dom/label #js {:onDoubleClick #(mut/toggle! this :editing)} text)
          (dom/button #js {:className "destroy"
                           :onClick   #(delete-item id)}))
        (dom/input #js {:className "edit"
                        :ref       "edit_field"
                        :value     edit-text
                        :onChange  #(om/update-state! this assoc :edit-text (.. % -target -value))
                        :onKeyDown #(cond
                                     (is-enter? %) (submit-edit %)
                                     (is-escape? %) (do (om/update-state! this assoc :edit-text text)
                                                        (mut/toggle! this :editing)))
                        :onBlur    #(when editing (submit-edit %))})))))

(def ui-todo-item (om/factory TodoItem {:keyfn :id}))

(defui ^:once TodoList
  static om/IQuery
  (query [this] [{:todos (om/get-query TodoItem)}
                 :todos/num-completed
                 :todos/filter])
  Object
  (render [this]
    (let [{:keys [todos todos/num-completed todos/filter]} (om/props this)
          num-todos (count todos)
          delete-item (fn [item-id] (om/transact! this `[(todo/delete-item ~{:id item-id})]))
          all-completed? (= num-completed num-todos)
          filtered-todos (case filter
                           :active (filterv (comp not :completed) todos)
                           :completed (filterv :completed todos)
                           todos)]

      (dom/div nil
        (dom/div #js {:style #js {:position "fixed" :top "0" :right "0"} :className "support"}
          (dom/button #js { :onClick #(om/transact! this '[(todo/send-support-request)])} "Send Request"))
        (dom/section #js {:className "todoapp"}

          (.header this)

          (when (pos? num-todos)
            (dom/div nil
              (dom/section #js {:className "main"}
                (dom/input #js {:className "toggle-all"
                                :type      "checkbox"
                                :checked   all-completed?
                                :onClick   #(om/transact! this `[(todo/toggle-all ~{:all-completed? all-completed?})])})
                (dom/label #js {:htmlFor "toggle-all"} "Mark all as complete")
                (dom/ul #js {:className "todo-list"}
                  (map #(ui-todo-item (om/computed % {:onDelete delete-item})) filtered-todos)))

              (.filter-footer this))))

        (.footer-info this))))

  (header [this]
    (letfn [(add-item [evt]
              (when (is-enter? evt)
                (when-let [trimmed-text (trim-text (.. evt -target -value))]
                  (om/transact! this `[(todo/new-item ~{:text trimmed-text})])
                  (set! (.. evt -target -value) ""))))]

      (dom/header #js {:className "header"}
        (dom/h1 nil "todos")
        (dom/input #js {:className   "new-todo"
                        :placeholder "What needs to be done?"
                        :autoFocus   true
                        :onKeyDown   add-item}))))

  (filter-footer [this]
    (let [{:keys [todos todos/num-completed todos/filter]} (om/props this)
          num-todos (count todos)
          num-remaining (- num-todos num-completed)]

      (dom/footer #js {:className "footer"}
        (dom/span #js {:className "todo-count"}
          (dom/strong nil num-remaining)
          (str (if (= num-remaining 1) " item" " items") " left"))
        (dom/ul #js {:className "filters"}
          (dom/li nil
            (dom/a #js {:className (when (or (nil? filter) (= :none filter)) "selected")
                        :href      "#"} "All"))
          (dom/li nil
            (dom/a #js {:className (when (= :active filter) "selected")
                        :href      "#/active"} "Active"))
          (dom/li nil
            (dom/a #js {:className (when (= :completed filter) "selected")
                        :href      "#/completed"} "Completed")))
        (when (pos? num-completed)
          (dom/button #js {:className "clear-completed"
                           :onClick   #(om/transact! this `[(todo/clear-complete)])} "Clear Completed")))))

  (footer-info [this]
    (dom/footer #js {:className "info"}
      (dom/p nil "Double-click to edit a todo")
      (dom/p nil "Created by "
        (dom/a #js {:href   "http://www.thenavisway.com"
                    :target "_blank"} "NAVIS"))
      (dom/p nil "Part of "
        (dom/a #js {:href   "http://todomvc.com"
                    :target "_blank"} "TodoMVC")))))
