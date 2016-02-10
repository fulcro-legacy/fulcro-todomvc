(ns untangled-todomvc.todo-spec
  (:require [untangled-spec.core :refer-macros [specification assertions]]))

(specification "Foo bar baz"
  (assertions
    "Are tests working?"
    1 => 1))
