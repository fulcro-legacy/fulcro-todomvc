(ns untangled-todomvc.ui
  (:require [om.next :as om :refer-macros [defui]]
            [untangled.client.mutations :as mut]
            [om.dom :as dom]))

(defui TodoItem
  static om/IQuery
  (query [_] [:id :text :completed :editing])
  static om/Ident
  (ident [_ props] [:todo/by-id (:id props)])
  Object
  (render [this]
    (let [{:keys [id text completed]} (om/props this)
          onDelete (om/get-computed this :onDelete)]
      (dom/li #js {:className (if completed "completed" "")}
        (dom/div #js {:className "view"}
          (dom/input #js {:className "toggle"
                          :type      "checkbox"
                          :onChange  #(om/transact! this `[(todo/toggle-complete ~{:id id}) :todos/num-completed])})
          (dom/label nil text)
          (dom/button #js {:className "destroy"
                           :onClick   #(onDelete id)}))
        (dom/input #js {:className "edit"})))))

(def ui-todo-item (om/factory TodoItem {:keyfn :id}))

(defui TodoList
  static om/IQuery
  (query [this] [{:todos (om/get-query TodoItem)}
                 :todos/num-completed])
  Object
  (render [this]
    (let [{:keys [todos todos/num-completed]} (om/props this)
          num-todos (count todos)
          delete-item (fn [item-id] (om/transact! this `[(todo/delete-item ~{:id item-id})]))]
      (dom/div nil
        (dom/section #js {:className "todoapp"}

          (.header this)

          (when (pos? num-todos)
            (dom/div nil
              (dom/section #js {:className "main"}
                (dom/input #js {:className "toggle-all"
                                :type      "checkbox"
                                :checked   (= num-completed num-todos)
                                :onClick #(om/transact! this `[(todo/toggle-all) :todos/num-completed])})
                (dom/label #js {:htmlFor "toggle-all"} "Mark all as complete")
                (dom/ul #js {:className "todo-list"}
                  (map #(ui-todo-item (om/computed % {:onDelete delete-item})) todos)))

              (.filter-footer this))))

        (.footer-info this))))

  (header [this]
    (letfn [(is-enter? [evt] (= 13 (.-keyCode evt)))
            (make-new-item [evt]
              (let [text (clojure.string/trim (.. evt -target -value))]
                (when (and (is-enter? evt) (not (empty? text)))
                  (om/transact! this `[(todo/new-item ~{:text text})])
                  (set! (.. evt -target -value) ""))))]

      (dom/header #js {:className "header"}
        (dom/h1 nil "todos")
        (dom/input #js {:className   "new-todo"
                        :placeholder "What needs to be done?"
                        :autoFocus   true
                        :onKeyDown   make-new-item}))))

  (filter-footer [this]
    (let [{:keys [todos todos/num-completed]} (om/props this)
          num-todos (count todos)]
      (dom/footer #js {:className "footer"}
        (dom/span #js {:className "todo-count"}
          (dom/strong nil (- num-todos num-completed)) " items left")
        (dom/ul #js {:className "filters"}
          (dom/li nil
            (dom/a #js {:className "selected" :href "#"} "All")
            (dom/a #js {:href "#"} "Active")
            (dom/a #js {:href "#"} "Completed")))
        (when (pos? num-completed)
          (dom/button #js {:className "clear-completed"} "Clear Completed")))))


  (footer-info [this]
    (dom/footer #js {:className "info"}
      (dom/p nil "Double-click to edit a todo")
      (dom/p nil "Created by "
        (dom/a #js {:href   "http://www.thenavisway.com"
                    :target "_blank"} "NAVIS"))
      (dom/p nil "Part of "
        (dom/a #js {:href   "http://todomvc.com"
                    :target "_blank"} "TodoMVC")))))
