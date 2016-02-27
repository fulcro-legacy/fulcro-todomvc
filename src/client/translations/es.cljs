(ns translations.es
  (:require untangled.i18n.core))

(def translations
  {"|todos" "quehaceres"})

(swap!
  untangled.i18n.core/*loaded-translations*
  (fn [x] (assoc x "es-MX" translations)))
