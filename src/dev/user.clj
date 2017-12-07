(ns user
  (:require
    [clojure.tools.namespace.repl :as tools-ns :refer [set-refresh-dirs]]
    [com.stuartsierra.component :as component]
    [figwheel-sidecar.system :as fig]
    [fulcro-spec.suite :as suite]
    [fulcro-spec.selectors :as sel]
    [fulcro-todomvc.server :as server]))

;;FIGWHEEL
(def figwheel (atom nil))

; Usable from a REPL to start one-or-more figwheel builds. Support -Dbuild-name and -Dfigwheel.port so that you can
; run any specific build(s) on a custom port from an IntelliJ Run Configuration. This is helpful when you want
; to run multiple builds, but want fast hot reload (place each build on a separate figwheel instance running on
; a custom port. E.g. in JVM Args: `-Dcards -Dfigwheel.port=5000` with parameters `script/figwheel.clj`.
(defn start-figwheel
  "Start Figwheel on the given builds, or defaults to build-ids in `figwheel-config`."
  ([]
   (let [figwheel-config (fig/fetch-config)
         props           (System/getProperties)
         all-builds      (->> figwheel-config :data :all-builds (mapv :id))]
     (start-figwheel (keys (select-keys props all-builds)))))
  ([build-ids]
   (let [figwheel-config   (fig/fetch-config)
         port              (some-> (System/getProperty "figwheel.port") Integer/parseInt)
         default-build-ids (-> figwheel-config :data :build-ids)
         build-ids         (if (empty? build-ids) default-build-ids build-ids)
         preferred-config  (cond-> (assoc-in figwheel-config [:data :build-ids] build-ids)
                             (and port (pos? port)) (assoc-in [:data :figwheel-options :server-port] port))]
     (reset! figwheel (component/system-map
                        :css-watcher (fig/css-watcher {:watch-paths ["resources/public/css"]})
                        :figwheel-system (fig/figwheel-system preferred-config)))
     (println "STARTING FIGWHEEL ON BUILDS: " build-ids)
     (swap! figwheel component/start)
     (fig/cljs-repl (:figwheel-system @figwheel)))))

;; ==================== SERVER ====================

(set-refresh-dirs "src/cards" "src/dev" "src/main" "src/test")

(defn started? [sys]
  (-> sys :config :value))

(defonce system (atom nil))

(defn- refresh [& args]
  {:pre [(not @system)]}
  (apply tools-ns/refresh args))

(defn- init []
  {:pre [(not (started? @system))]}
  (when-let [new-system (server/make-system)]
    (reset! system new-system)))

(defn- start []
  {:pre [@system (not (started? @system))]}
  (swap! system component/start))

(defn stop
  "Stop the server."
  []
  (when (started? @system)
    (swap! system component/stop))
  (reset! system nil))

(defn go
  "Initialize the server and start it."
  []
  {:pre [(not @system) (not (started? @system))]}
  (init)
  (start))

(defn restart
  "Stop, refresh, and restart the server."
  []
  (stop)
  (refresh :after 'user/go))

; Run (start-server-tests) in a REPL to start a runner that can render results in a browser
(suite/def-test-suite start-server-tests
  {:config       {:port 8888}
   :test-paths   ["src/test"]
   :source-paths ["dev/server" "src/main"]}
  {:available #{:focused :unit :integration}
   :default   #{::sel/none :focused :unit}})

