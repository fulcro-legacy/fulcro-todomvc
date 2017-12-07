(ns fulcro-todomvc.server
  (:require
    [fulcro.easy-server :as easy]
    [fulcro.server :as server]
    fulcro-todomvc.api                                      ; ensure the mutations and queries are loaded
    [fulcro.datomic.core :refer [build-database]]))

(defn make-system
  ([] (make-system "config/dev.edn"))
  ([config-path]
   (let [config-path config-path]                           ; in production, should use a filesystem file
     (easy/make-fulcro-server
       :config-path config-path
       :parser (server/fulcro-parser)                       ; allows us to use built-in multimethods and helper macros for reads/mutates
       :parser-injections #{:todo-database}                 ; places the named components into the env of mutations and queries
       :components {:todo-database (build-database :todo)   ; adds components to the server
                    :logger        {}}))))
