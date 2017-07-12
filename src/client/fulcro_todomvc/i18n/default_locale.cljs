(ns fulcro-todomvc.i18n.default-locale (:require fulcro-todomvc.i18n.en-US [fulcro.i18n.core :as i18n]))

(reset! i18n/*current-locale* "en-US")

(swap! i18n/*loaded-translations* #(assoc % :en-US fulcro-todomvc.i18n.en-US/translations))