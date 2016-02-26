(ns untangled-todomvc.history
  (:require [cljs.reader :as reader]))

(def storage-key "app-history")

(defn get-storage []
  ((fnil reader/read-string "") (.getItem js/localStorage storage-key)))

(defn set-storage! [val]
  (->> val pr-str (.setItem js/localStorage storage-key)))

(defn serialize-history [untangled-app]
  (let [history-steps (-> untangled-app :reconciler :config :history .-arr)
        history-map (-> untangled-app :reconciler :config :history .-index deref)]
    {:steps   history-steps
     :history (into {} (map (fn [[k v]]
                              (js/console.log k "entry metadata: " (meta v))
                              [k (assoc v :untangled/meta (meta v))]) history-map))}))

