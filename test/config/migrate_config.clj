(ns config.migrate-config)

(def version (atom nil))

(defn memory-current-db-version []
  (or @version 0)) 

(defn memory-update-db-version [new-version]
  (swap! version #(identity %2) new-version)) 

(defn migrate-config []
  { :directory "/test/migrations"
    :current-db-version memory-current-db-version
    :update-db-version memory-update-db-version })