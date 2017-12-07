(ns fulcro-todomvc.intro
  (:require [devcards.core :as rc :refer-macros [defcard]]
            [fulcro.client.dom :as dom]))

(defcard Placeholder
  "# Placeholder"
  (dom/div nil "TODO"))
