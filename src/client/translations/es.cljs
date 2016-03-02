(ns translations.es
  (:require [untangled.i18n.core :refer [*loaded-translations*]]))

(def translations
  {"|todos"                       "quehaceres"
   "|item"                        "tarea"
   "|items"                       "tareas"
   "|left"                        "quedando"
   "|What needs to be done?"      "¿Que se necesita hacer?"
   "|Completed"                   "Completado"
   "|Active"                      "Activo"
   "|All"                         "Todo"
   "|Clear Completed"             "Quitar Completado"
   "|Send Request"                "Mandar Pedido"
   "|Double-click to edit a todo" "Haz doble clic en tarea para editárla."
   "|Created by "                 "Hecho por "
   "|Part of "                    "Parte de "
   })

(swap! *loaded-translations* (fn [x] (assoc x "es" translations)))
