(ns todomvc.system
  (:require
    [untangled.server.core :as core]
    [todomvc.api :as api]
    [om.next.server :as om]
    [taoensso.timbre :as timbre]
    [untangled.datomic.core :refer [build-database]]
    [untangled.server.core :as c]))

(defn logging-mutate [env k params]
  (timbre/info "Mutation Request: " k)
  (api/apimutate env k params))

(defn make-system []
  (let [config-path "/usr/local/etc/todomvc.edn"]
    (core/make-untangled-server
      :config-path config-path
      :parser (om/parser {:read api/api-read :mutate logging-mutate})
      :parser-injections #{:todo-database}
      :components {:todo-database (build-database :todo)
                   :logger        {}})))
