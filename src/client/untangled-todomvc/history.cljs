(ns untangled-todomvc.history
  (:require [cljs.reader :as reader]))

(defn serialize-history [untangled-app]
  (let [history-steps (-> untangled-app :reconciler :config :history .-arr)
        history-map (-> untangled-app :reconciler :config :history .-index deref)]
    {:steps   history-steps
     :history (into {} (map (fn [[k v]]
                              [k (assoc v :untangled/meta (meta v))]) history-map))}))

