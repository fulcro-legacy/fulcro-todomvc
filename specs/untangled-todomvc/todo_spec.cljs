(ns untangled-todomvc.todo-spec
  (:require
    untangled-todomvc.mutations
    [untangled.client.mutations :as m]
    [untangled-spec.core :refer-macros [specification behavior assertions when-mocking]]
    [om.next :as om]))

(specification "Adding a todo"
  (let [state (atom {})]
    (when-mocking
      (om/tempid) => :new-id
      ((:action (m/mutate {:state state} 'todo/new-item {:text "Hello"}))))

    (assertions
      "Adds an ident to list of todos"
      (:todos @state) => [[:todo/by-id :new-id]]
      "Adds todo data at the ident in the app-state"
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
