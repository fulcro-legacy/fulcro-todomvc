(ns fulcro-todomvc.server-main
  (:require
    [com.stuartsierra.component :as component]
    [fulcro.server :as c]
    [fulcro-todomvc.server :refer [make-system]])
  (:gen-class))

; It is recommended that your production config be on the server itself in a directory. We have placed a
; sample on the classpath, which is also legal:
(def config-path "config/prod.edn")

;; This is a separate file for the uberjar only. We control the server in dev mode from src/dev/user.clj
(defn -main [& args]
  (let [system (make-system config-path)]
    (component/start system)))
