(ns untangled-todomvc.storage-spec
  (:require
    [untangled-spec.core :refer-macros [specification behavior assertions when-mocking]]
    [cljs.test :refer-macros [is]]
    [untangled-todomvc.storage :as storage]
    [cognitect.transit :as t]))

(specification "Local storage"
  (let [data {:foo     "bar"
              :baz     1
              'a-thing [:some "other" :stuff]}
        storage-key storage/storage-key]

    (set! storage/storage-key "untangled.test")

    (behavior "Stores and retrieves data"
      (storage/set-storage! data)
      (is (= data (storage/get-storage))))

    (behavior "Clears data"
      (storage/clear-storage!)
      (is (nil? (storage/get-storage))))

    (storage/clear-storage!)
    (set! storage/storage-key storage-key)))

