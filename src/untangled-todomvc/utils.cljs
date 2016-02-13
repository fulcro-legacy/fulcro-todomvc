(ns untangled-todomvc.utils
  (:require
    [cognitect.transit :as t]
    [untangled.client.logging :as log]))

(def storage-key "todos-untangled")

(def reader (t/reader :json))
(def writer (t/writer :json))

(log/set-level :debug)

(defn get-storage []
  (->> storage-key (.getItem (.-localStorage js/window)) (t/read reader)))

(defn set-storage! [val]
  (->> val (t/write writer) (.setItem (.-localStorage js/window) storage-key)))

(defn update-storage! [f]
  (set-storage! (f (get-storage))))

(defn clear-storage! []
  (->> storage-key (.removeItem (.-localStorage js/window))))
