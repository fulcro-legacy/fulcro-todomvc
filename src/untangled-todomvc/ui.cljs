(ns untangled-todomvc.ui
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defui Root
  Object
  (render [this]
    (dom/div nil "Hello, Ethan!")))
