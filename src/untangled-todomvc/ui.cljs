(ns untangled-todomvc.ui
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defui TodoItem
  static om/IQuery
  (query [_] [:id :text :completed :editing])
  static om/Ident
  (ident [_ props] [:todo/by-id (:id props)])
  Object
  (render [this]
    (dom/li nil
      (dom/div #js {:className "view"}
        (dom/input #js {:className "toggle" :type "checkbox"})
        (dom/label nil "Remember the milk")
        (dom/button #js {:className "destroy"}))
      (dom/input #js {:className "edit"}))))

(def ui-todo-item (om/factory TodoItem))

(defui TodoList
  static om/IQuery
  (query [this] [{:todos (om/get-query TodoItem)}])
  Object
  (render [this]
    (let [{:keys [todos]} (om/props this)]
      (dom/div nil
        (dom/section #js {:className "todoapp"}
          ;; Header
          (dom/header #js {:className "header"}
            (dom/h1 nil "todos")
            (dom/input #js {:className   "new-todo"
                            :placeholder "What needs to be done?"
                            :autoFocus   true}))

          ;; Todo List
          (dom/section #js {:className "main"}
            (dom/input #js {:className "toggle-all" :type "checkbox"})
            (dom/label #js {:htmlFor "toggle-all"} "Mark all as complete")
            (dom/ul #js {:className "todo-list"}
              (ui-todo-item todos)))

          ;; Filter footer
          (dom/footer #js {:className "footer"}
            (dom/span #js {:className "todo-count"}
              (dom/strong nil "0") " items left")
            (dom/ul #js {:className "filters"}
              (dom/li nil
                (dom/a #js {:className "selected" :href "#"} "All")
                (dom/a #js {:href "#"} "Active")
                (dom/a #js {:href "#"} "Completed")))))

        (dom/footer #js {:className "info"}
          (dom/p nil "Double-click to edit a todo")
          (dom/p nil "Created by " (dom/a #js {:href "http://www.thenavisway.com"} "NAVIS"))
          (dom/p nil "Part of " (dom/a #js {:href "http://todomvc.com"} "TodoMVC")))))))
