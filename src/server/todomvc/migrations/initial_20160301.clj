(ns todomvc.migrations.initial-20160301
  (:require [datomic.api :as d]
            [untangled.datomic.schema :as s]))

(defn transactions []
  [(s/generate-schema
     [(s/schema list
        (s/fields
          [title :string :unique-identity "The title of the todo list."]
          [filter :enum [:none :completed :active]]
          [items :ref :many :component "Items on the list."]))

      (s/schema item
        (s/fields
          [label :string "Item label"]
          [complete :boolean "Is the item complete?"]))])])
