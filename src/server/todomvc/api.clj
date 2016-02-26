(ns todomvc.api
  (:require [om.next.server :as om]
            [taoensso.timbre :as timbre]))

(defmulti apimutate om/dispatch)

(defmethod apimutate :default [e k p]
  (timbre/error "Unrecognized mutation: " k p))

(defonce last-id (atom 1000))
(defonce requests (atom {}))

(defmethod apimutate 'support-viewer/send-support-request [e k p]
  (let [_ (swap! last-id inc)
        id @last-id]
    {:action
     (fn []
       (timbre/info "New support request " id)
       (swap! requests assoc id p)
       id)}))

(defn ensure-integer [n]
  (cond
    (string? n) (Integer/parseInt n)
    :else n))

(defn api-read [env k params]
  (case k
    :support-request (let [id (:id params)]
                       (timbre/info "Sending history for " id)
                       {:value (get @requests (ensure-integer id))})
    (timbre/error "Unrecognized read: " k)))
