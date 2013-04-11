(ns drift.version
  (:require [drift.config :as config]))

(defn
#^{ :doc "Returns the current db version." }
  current-db-version [] 
  ((config/current-version-fn)))

(defn
#^{ :doc "Sets the db version." }
  update-db-version [new-version] 
  ((config/update-version-fn) new-version)
  new-version)
