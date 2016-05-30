(ns untangled-todomvc.todo-spec
  (:require
    untangled-todomvc.mutations
    [untangled.client.mutations :as m]
    [untangled-spec.core :refer-macros [specification behavior assertions when-mocking]]
    [untangled.dom :refer [unique-key]]
    [om.next :as om]))

(specification "Adding a todo."
  (let [state (atom {})]
    ((:action (m/mutate {:state state} 'todo/new-item {:id :new-id :text "Hello"})))

    (assertions
      "Adds an ident to list of todos."
      (-> @state :todos :list/items) => [[:todo/by-id :new-id]]
      "Adds todo data at the ident in the app-state."
      (-> @state :todo/by-id :new-id) => {:db/id :new-id :item/label "Hello"})))

(specification "Toggling a todo's completion"
  (let [to-complete-state (atom {:todos      {:list/items [[:todo/by-id 1]]}
                                 :todo/by-id {1 {:db/id 1 :text "Hello"}}})
        to-active-state (atom {:todos      {:list/items [[:todo/by-id 1]]}
                               :todo/by-id {1 {:db/id 1 :text "Hello" :item/complete true}}})]

    (behavior "when marking complete"
      ((:action (m/mutate {:state to-complete-state} 'todo/check {:id 1})))
      (assertions
        "marks todo as completed."
        (get-in @to-complete-state [:todo/by-id 1 :item/complete]) => true))

    (behavior "when marking incomplete"
      ((:action (m/mutate {:state to-active-state} 'todo/uncheck {:id 1})))
      (assertions
        "marks todo as active."
        (get-in @to-active-state [:todo/by-id 1 :item/complete]) => false))))

(specification "Editing a todo"
  (let [state (atom {:todos      {:list/items [[:todo/by-id 1]]}
                     :todo/by-id {1 {:db/id 1 :item/label "Hello"}}})]

    ((:action (m/mutate {:state state} 'todo/edit {:id 1 :text "Goodbye"})))
    (assertions
      "changes the text for that todo in the app state."
      (get-in @state [:todo/by-id 1 :item/label]) => "Goodbye")))

(specification "Toggling the completion of all todos"
  (let [to-complete-state (atom {:todos      {:list/items [[:todo/by-id 1] [:todo/by-id 2]]}
                                 :todo/by-id {1 {:id 1 :text "Hello"}
                                              2 {:id 2 :text "Bye" :completed true}}})

        to-active-state (atom {:todos      {:list/items [[:todo/by-id 1] [:todo/by-id 2]]}
                               :todo/by-id {1 {:id 1 :text "Hello" :completed true}
                                            2 {:id 2 :text "Bye" :completed true}}})]

    (behavior "when setting all to complete status"
      ((:action (m/mutate {:state to-complete-state} 'todo/check-all {})))

      (assertions
        "marks all todo items as complete."
        (vals (:todo/by-id @to-complete-state)) =fn=>
        (fn [todos]
          (when (some? todos)
            (reduce
              (fn [acc todo] (and acc (:item/complete todo))) true todos)))))

    (behavior "when un-checking all"
      ((:action (m/mutate {:state to-active-state} 'todo/uncheck-all {})))

      (assertions
        "marks all todo items as unchecked (active)."
        (vals (:todo/by-id @to-active-state)) =fn=>
        (fn [todos]
          (when (some? todos)
            (not
              (reduce
                (fn [acc todo] (or acc (:item/complete todo))) false todos))))))))

(specification "Clear completed todos"
  (let [state (atom {:todos      {:list/items [[:todo/by-id 1] [:todo/by-id 2]]}
                     :todo/by-id {1 {:db/id 1 :item/label "Hello"}
                                  2 {:db/id 2 :item/label "Bye" :item/complete true}}})]

    ((:action (m/mutate {:state state} 'todo/clear-complete {})))
    (assertions
      "removes completed todos from app state."
      (-> @state :todos :list/items) => [[:todo/by-id 1]])))

(specification "Can change the filter"
  (let [state (atom {})]
    ((:action (m/mutate {:state state} 'todo/filter {:filter :my-filter})))
    (assertions
      (-> @state :todos :list/filter) => :my-filter)))

(specification "Delete todo"
  (let [state (atom {:todos      {:list/items [[:todo/by-id 1] [:todo/by-id 2]]}
                     :todo/by-id {1 {:id 1 :text "Hello"}
                                  2 {:id 2 :text "Bye" :completed true}}})]

    ((:action (m/mutate {:state state} 'todo/delete-item {:id 1})))
    (assertions
      "deletes todo from app state."
      (-> @state :todos :list/items) => [[:todo/by-id 2]]
      (:todo/by-id @state) => {2 {:id 2 :text "Bye" :completed true}})))
