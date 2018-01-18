(ns fulcro-todomvc.todo-spec
  (:require
    [fulcro-todomvc.api :as api]
    [fulcro.client.mutations :as m]
    [fulcro-spec.core :refer [specification behavior assertions when-mocking]]
    [fulcro.util :refer [unique-key]]
    [fulcro.client.primitives :as om]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; NOTE: The ^:intern metadata on the mutations causes the macro to add a function to
;; the namespace that can be called normally, with the first argument being the `env`.
;; We're using that below to write our tests, so we don't have to test against the
;; multimethod (which can also be done).
;; IDEs will show the function calls as errors (because the arity looks wrong),
;; but the calls will succeed.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(specification "Adding a todo."
  (let [state (atom {})]
    (api/todo-new-item {:state state} {:list-id :main :id :new-id :text "Hello"})

    (assertions
      "Adds an ident to list of todos."
      (-> @state :list/by-id :main :list/items) => [[:todo/by-id :new-id]]
      "Adds todo data at the ident in the app-state."
      (-> @state :todo/by-id :new-id) => {:db/id :new-id :item/label "Hello"})))

(specification "Toggling a todo's completion"
  (let [to-complete-state (atom {:todos      {:list/items [[:todo/by-id 1]]}
                                 :todo/by-id {1 {:db/id 1 :text "Hello"}}})
        to-active-state   (atom {:todos      {:list/items [[:todo/by-id 1]]}
                                 :todo/by-id {1 {:db/id 1 :text "Hello" :item/complete true}}})]

    (behavior "when marking complete"

      (api/todo-check {:state to-complete-state} {:id 1})

      (assertions
        "marks todo as completed."
        (get-in @to-complete-state [:todo/by-id 1 :item/complete]) => true))

    (behavior "when marking incomplete"

      (api/todo-uncheck {:state to-active-state} {:id 1})

      (assertions
        "marks todo as active."
        (get-in @to-active-state [:todo/by-id 1 :item/complete]) => false))))

(specification "Editing a todo"
  (let [state (atom {:todos      {:list/items [[:todo/by-id 1]]}
                     :todo/by-id {1 {:db/id 1 :item/label "Hello"}}})]

    (api/commit-label-change {:state state} {:id 1 :text "Goodbye"})

    (assertions
      "changes the text for that todo in the app state."
      (get-in @state [:todo/by-id 1 :item/label]) => "Goodbye")))

(specification "Toggling the completion of all todos"
  (let [to-complete-state (atom {:list/by-id {:main {:list/items [[:todo/by-id 1] [:todo/by-id 2]]}}
                                 :todo/by-id {1 {:id 1 :text "Hello"}
                                              2 {:id 2 :text "Bye" :completed true}}})

        to-active-state   (atom {:list/by-id {:main {:list/items [[:todo/by-id 1] [:todo/by-id 2]]}}
                                 :todo/by-id {1 {:id 1 :text "Hello" :completed true}
                                              2 {:id 2 :text "Bye" :completed true}}})]

    (behavior "when setting all to complete status"
      (api/todo-check-all {:state to-complete-state} {:list-id :main})

      (assertions
        "marks all todo items as complete."
        (vals (:todo/by-id @to-complete-state)) =fn=> (fn [items] (every? :item/complete items))))

    (behavior "when un-checking all"
      (api/todo-uncheck-all {:state to-active-state} {:list-id :main})

      (assertions
        "marks all todo items as unchecked (active)."
        (vals (:todo/by-id @to-active-state)) =fn=> (fn [items] (every? #(not (:item/complete %)) items))))))

(specification "Clear completed todos"
  (let [state (atom {:list/by-id {:main {:list/items [[:todo/by-id 1] [:todo/by-id 2]]}}
                     :todo/by-id {1 {:db/id 1 :item/label "Hello"}
                                  2 {:db/id 2 :item/label "Bye" :item/complete true}}})]

    (api/todo-clear-complete {:state state} {:list-id :main})

    (assertions
      "removes completed todos from app state."
      (-> @state :list/by-id :main :list/items) => [[:todo/by-id 1]])))

(specification "filtering"
  (behavior "on startup, caches the desired filter"
    (let [state (atom {})]

      (api/todo-filter {:state state} {:filter :my-filter})

      (assertions
        (-> @state :root/desired-filter) => :my-filter)))
  (behavior "then the set-desired-filter post mutation"
    (let [state (atom {:application         {:root {:todos [:list/by-id 1]}}
                       :root/desired-filter :active
                       :list/by-id          {1 {:db/id 1}}})]

      (api/set-desired-filter {:state state} {})

      (assertions
        "copies the desired filter onto the now-loaded list."
        (get-in @state [:list/by-id 1 :list/filter]) => :active
        "removes the desire."
        (-> @state :root/desired-filter) => nil)))
  (behavior "during normal operation, it uses :todos to find the active list and applies the filter"
    (let [state (atom {:application {:root {:todos [:list/by-id 1]}}
                       :list/by-id  {1 {:db/id 1}}})]

      (api/todo-filter {:state state} {:filter :inactive})

      (assertions
        (get-in @state [:list/by-id 1 :list/filter]) => :inactive))))

(specification "Delete todo"
  (let [state (atom {:list/by-id {:main {:list/items [[:todo/by-id 1] [:todo/by-id 2]]}}
                     :todo/by-id {1 {:id 1 :text "Hello"}
                                  2 {:id 2 :text "Bye" :completed true}}})]

    (api/todo-delete-item {:state state} {:list-id :main :id 1})

    (assertions
      "deletes todo from app state."
      (-> @state :list/by-id :main :list/items) => [[:todo/by-id 2]]
      (:todo/by-id @state) => {2 {:id 2 :text "Bye" :completed true}})))
