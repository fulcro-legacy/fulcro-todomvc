(ns untangled-todomvc.utils
  (:require
    [cljs.reader :as reader]
    [untangled.client.logging :as log]))

(def storage-key "todos-untangled")

(defn get-storage []
  ((fnil reader/read-string "") (.getItem js/localStorage storage-key)))

(defn set-storage! [val]
  (->> val pr-str (.setItem js/localStorage storage-key)))

(defn update-storage! [f]
  (set-storage! (f (get-storage))))

(defn clear-storage! []
  (->> storage-key (.removeItem js/localStorage)))

