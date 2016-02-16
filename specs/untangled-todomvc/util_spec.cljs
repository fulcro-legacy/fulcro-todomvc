(ns untangled-todomvc.util-spec
  (:require
    [untangled-spec.core :refer-macros [specification behavior assertions when-mocking]]
    [cljs.test :refer-macros [is]]
    [untangled-todomvc.utils :as util]
    [cognitect.transit :as t]))

(specification "Local storage"
  (let [data {:foo     "bar"
              :baz     1
              'a-thing [:some "other" :stuff]}
        storage-key util/storage-key]

    (set! util/storage-key "untangled.test")

    (behavior "Stores and retrieves data"
      (util/set-storage! data)
      (is (= data (util/get-storage))))

    (behavior "Clears data"
      (util/clear-storage!)
      (is (nil? (util/get-storage))))

    (util/clear-storage!)
    (set! util/storage-key storage-key)))

