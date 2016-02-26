(ns untangled-todomvc.storage
  (:require [cljs.reader :as reader]))

(def storage-key "todos-untangled")

(defn get-storage []
  ((fnil reader/read-string "") (.getItem js/localStorage storage-key)))

(defn set-storage! [val]
  (->> val pr-str (.setItem js/localStorage storage-key)))

(defn clear-storage! []
  (->> storage-key (.removeItem js/localStorage)))

