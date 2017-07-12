(ns todomvc.system
  (:require
    [fulcro.server.core :as core]
    [todomvc.api :as api]
    [om.next.server :as om]
    [taoensso.timbre :as timbre]
    [fulcro.datomic.core :refer [build-database]]
    [fulcro.server.core :as c]))

(defn logging-mutate [env k params]
  (timbre/info "Mutation Request: " k)
  (api/apimutate env k params))

(defn make-system []
  (let [config-path "/usr/local/etc/todomvc.edn"]
    (core/make-fulcro-server
      :config-path config-path
      :parser (om/parser {:read api/api-read :mutate logging-mutate})
      :parser-injections #{:todo-database}
      :components {:todo-database (build-database :todo)
                   :logger        {}})))
