(ns fulcro-todomvc.i18n.en-US (:require fulcro.i18n.core) (:import goog.module.ModuleManager))

;; This file was generated by fulcro's i18n leiningen plugin.

(def translations {})

(swap!
 fulcro.i18n.core/*loaded-translations*
 (fn [x] (assoc x "en-US" translations)))

(try
 (-> goog.module.ModuleManager .getInstance (.setLoaded "en-US"))
 (catch js/Object obj))
