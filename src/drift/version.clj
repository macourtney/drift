(ns drift.version
  (:require [drift.core :as core]))

(defn
#^{ :doc "Returns the current db version." }
  current-db-version [] 
  ((:current-db-version (core/find-config))))

(defn
#^{ :doc "Sets the db version." }
  update-db-version [new-version] 
  ((:update-db-version (core/find-config)) new-version))