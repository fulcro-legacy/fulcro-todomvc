(ns todomvc.system
  (:require
    [untangled.server.core :as core]
    [todomvc.api :as api]
    [om.next.server :as om]
    [taoensso.timbre :as timbre]))

;; IMPORTANT: Remember to load all multi-method namespaces to ensure all of the methods are defined in your parser!

(defn logging-mutate [env k params]
  ; NOTE: Params can be a security/pci concern, so don't log them here.
  ; TODO: Include user info from env in logs.
  (timbre/info "Mutation Request: " k)
  (api/apimutate env k params))

(defn make-system []
  (let [config-path "config/defaults.edn"]
    (core/make-untangled-server
      :config-path config-path
      :parser (om/parser {:read api/api-read :mutate logging-mutate})
      :parser-injections #{}
      :components {})))
