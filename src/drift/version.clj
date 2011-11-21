(ns drift.version
  (:require [drift.core :as core]))

(defn
#^{ :doc "Returns the current db version." }
  current-db-version [] 
  ((:current-version (core/find-config))))

(defn
#^{ :doc "Sets the db version." }
  update-db-version [new-version] 
  ((:update-version (core/find-config)) new-version)
  new-version)