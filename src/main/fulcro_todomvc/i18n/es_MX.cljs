(ns fulcro-todomvc.i18n.es-MX
  (:require fulcro.i18n))

(def
  translations
  {"|Created by "                    "Creado por",
   "|Completed"                      "Completo",
   "|<===>"                          "",
   "|Active"                         "Activo",
   "|Frame {f,number} of {end,number} "
                                     "Marco {f,number} of {end,number} ",
   "|Double-click to edit a todo"    "Haga doble clic para editar un TODO",
   "|What needs to be done?"         "¿Qué es lo que se debe hacer?",
   "|All"                            "Todas",
   "|Help!"                          "¡Ayuda!",
   "|<Back"                          "Atrás",
   "|{num,plural,=0 {no items} =1 {1 item} other {# items}} left"
                                     "{num,plural,=0 {no queda nada} =1 {1 artículo restante} other {# artículos}}",
   "|{ts,date,short} {ts,time,long}" "",
   "|Mark all as complete"           "Marcar todo como completa",
   "|Send Request"                   "Enviar petición",
   "|Forward>"                       "Adelante >",
   "|Clear Completed"                "Despejado completo"})


(swap! fulcro.i18n/*loaded-translations* assoc :es-MX translations)
