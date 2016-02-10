(ns untangled-todomvc.todo-spec
  (:require
    untangled-todomvc.mutations
    [untangled.client.mutations :as m]
    [untangled-spec.core :refer-macros [specification behavior assertions when-mocking]]
    [om.next :as om]))

(specification "Adding a todo."
  (let [state (atom {})]
    (when-mocking
      (om/tempid) => :new-id
      ((:action (m/mutate {:state state} 'todo/new-item {:text "Hello"}))))

    (assertions
      "Adds an ident to list of todos."
      (:todos @state) => [[:todo/by-id :new-id]]
      "Adds todo data at the ident in the app-state."
      (:todo/by-id @state) => {:new-id {:id :new-id :text "Hello"}})))

(specification "Toggling a todo's completion"
  (let [to-complete-state (atom {:todos      [[:todo/by-id 1]]
                                 :todo/by-id {1 {:id 1 :text "Hello"}}})
        to-active-state (atom {:todos               [[:todo/by-id 1]]
                               :todo/by-id          {1 {:id 1 :text "Hello" :completed true}}
                               :todos/num-completed 1})]

    (behavior "when toggling to complete"
      ((:action (m/mutate {:state to-complete-state} 'todo/toggle-complete {:id 1})))
      (assertions
        "increments global count of completed todos."
        (:todos/num-completed @to-complete-state) => 1
        "marks todo as completed."
        (get-in @to-complete-state [:todo/by-id 1 :completed]) => true))

    (behavior "when toggling to active"
      ((:action (m/mutate {:state to-active-state} 'todo/toggle-complete {:id 1})))
      (assertions
        "decrements global count of completed todos."
        (:todos/num-completed @to-active-state) => 0
        "marks todo as active."
        (get-in @to-active-state [:todo/by-id 1 :completed]) => false))))

(specification "Toggling the completion of all todos"
  (let [to-complete-state (atom {:todos               [[:todo/by-id 1]]
                                 :todo/by-id          {1 {:id 1 :text "Hello"}
                                                       2 {:id 2 :text "Bye" :completed true}}
                                 :todos/num-completed 1})

        to-active-state (atom {:todos               [[:todo/by-id 1]]
                               :todo/by-id          {1 {:id 1 :text "Hello" :completed true}
                                                     2 {:id 2 :text "Bye" :completed true}}
                               :todos/num-completed 2})]

    (behavior "when toggling to complete status"
      ((:action (m/mutate {:state to-complete-state} 'todo/toggle-all {:all-completed? false})))

      (assertions
        "sets the number of completed todos to the number of todos."
        (:todos/num-completed @to-complete-state) => (count (:todos @to-complete-state))

        "marks all todo items as complete."
        (vals (:todo/by-id @to-complete-state)) =fn=>
        (fn [todos]
          (when (some? todos)
            (reduce
              (fn [acc todo] (and acc (:completed todo))) true todos)))))

    (behavior "when toggling to active status"
      ((:action (m/mutate {:state to-active-state} 'todo/toggle-all {:all-completed? true})))

      (assertions
        "sets the number of completed todos to 0."
        (:todos/num-completed @to-active-state) => 0
        "marks all todo items as active."
        (vals (:todo/by-id @to-active-state)) =fn=>
        (fn [todos]
          (when (some? todos)
            (not
              (reduce
                (fn [acc todo] (or acc (:completed todo))) false todos))))))))
