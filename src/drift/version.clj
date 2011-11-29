(ns drift.version
  (:require [drift.core :as core]))

(defn
#^{ :doc "Returns the current db version." }
  current-db-version [] 
  ((get (core/find-config) :current-version)))

(defn
#^{ :doc "Sets the db version." }
  update-db-version [new-version] 
  ((get (core/find-config) :update-version) new-version)
  new-version)