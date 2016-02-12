(ns untangled-todomvc.utils
  (:require
    [cljs.reader :as reader]
    [untangled.client.logging :as log]))

(def storage-key "todos-untangled")

(log/set-level :debug)

(defn set-storage! [val]
  (-> (.-localStorage js/window) (.setItem storage-key val)))

(defn update-storage! [f]
  (-> (.-localStorage js/window) (.setItem storage-key (log/debug (f (reader/read-string val))))))

(defn get-storage! []
  (-> (.-localStorage js/window) (.getItem storage-key) reader/read-string))

(defn clear-storage! []
  (-> (.-localStorage js/window) (.removeItem storage-key)))
