(ns untangled-todomvc.todo-spec
  (:require
    untangled-todomvc.mutations
    [untangled.client.mutations :as m]
    [untangled-todomvc.storage :as storage]
    [untangled-spec.core :refer-macros [specification behavior assertions when-mocking]]
    [untangled.dom :refer [unique-key]]))

(specification "Adding a todo."
  (let [state (atom {})]
    (when-mocking
      (unique-key) => :new-id

      ((:action (m/mutate {:state state} 'todo/new-item {:text "Hello"})))

      (assertions
        "Adds an ident to list of todos."
        (:todos @state) => [[:todo/by-id :new-id]]
        "Adds todo data at the ident in the app-state."
        (:todo/by-id @state) => {:new-id {:id :new-id :text "Hello"}}))))

(specification "Toggling a todo's completion"
  (let [to-complete-state (atom {:todos      [[:todo/by-id 1]]
                                 :todo/by-id {1 {:id 1 :text "Hello"}}})
        to-active-state (atom {:todos      [[:todo/by-id 1]]
                               :todo/by-id {1 {:id 1 :text "Hello" :completed true}}})]

    (behavior "when toggling to complete"
      ((:action (m/mutate {:state to-complete-state} 'todo/toggle-complete {:id 1})))
      (assertions
        "marks todo as completed."
        (get-in @to-complete-state [:todo/by-id 1 :completed]) => true))

    (behavior "when toggling to active"
      ((:action (m/mutate {:state to-active-state} 'todo/toggle-complete {:id 1})))
      (assertions
        "marks todo as active."
        (get-in @to-active-state [:todo/by-id 1 :completed]) => false))))

(specification "Editing a todo"
  (let [state (atom {:todos      [[:todo/by-id 1]]
                     :todo/by-id {1 {:id 1 :text "Hello"}}})]

    ((:action (m/mutate {:state state} 'todo/edit {:id 1 :text "Goodbye"})))
    (assertions
      "changes the text for that todo in the app state."
      (get-in @state [:todo/by-id 1 :text]) => "Goodbye")))

(specification "Toggling the completion of all todos"
  (let [to-complete-state (atom {:todos      [[:todo/by-id 1] [:todo/by-id 2]]
                                 :todo/by-id {1 {:id 1 :text "Hello"}
                                              2 {:id 2 :text "Bye" :completed true}}})

        to-active-state (atom {:todos      [[:todo/by-id 1] [:todo/by-id 2]]
                               :todo/by-id {1 {:id 1 :text "Hello" :completed true}
                                            2 {:id 2 :text "Bye" :completed true}}})]

    (behavior "when toggling to complete status"
      ((:action (m/mutate {:state to-complete-state} 'todo/toggle-all {:all-completed? false})))

      (assertions
        "marks all todo items as complete."
        (vals (:todo/by-id @to-complete-state)) =fn=>
        (fn [todos]
          (when (some? todos)
            (reduce
              (fn [acc todo] (and acc (:completed todo))) true todos)))))

    (behavior "when toggling to active status"
      ((:action (m/mutate {:state to-active-state} 'todo/toggle-all {:all-completed? true})))

      (assertions
        "marks all todo items as active."
        (vals (:todo/by-id @to-active-state)) =fn=>
        (fn [todos]
          (when (some? todos)
            (not
              (reduce
                (fn [acc todo] (or acc (:completed todo))) false todos))))))))

(specification "Clear completed todos"
  (let [state (atom {:todos      [[:todo/by-id 1] [:todo/by-id 2]]
                     :todo/by-id {1 {:id 1 :text "Hello"}
                                  2 {:id 2 :text "Bye" :completed true}}})]

    ((:action (m/mutate {:state state} 'todo/clear-complete {})))
    (assertions
      "removes completed todos from app state."
      (:todos @state) => [[:todo/by-id 1]]
      (:todo/by-id @state) => {1 {:id 1 :text "Hello"}})))

(specification "Can change the filter"
  (let [state (atom {})]
    ((:action (m/mutate {:state state} 'todo/filter {:filter :my-filter})))
    (assertions
      (:todos/filter @state) => :my-filter)))
