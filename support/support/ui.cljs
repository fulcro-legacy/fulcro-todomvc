(ns support.ui
  (:require
    [om.next :as om :refer-macros [defui]]
    [om.dom :as dom]))

(defui ^:once Root
  static om/IQuery
  (query [this] [:react-key])
  Object
  (render [this]
    (let [{:keys [react-key] :or {react-key "ROOT"}} (om/props this)]
      (dom/div #js {:key react-key :style #js {:position "absolute" :left "50px" :top "50px"}}
        (dom/button nil "<Back")
        " "
        (dom/button nil "Forward>")
        ))))
