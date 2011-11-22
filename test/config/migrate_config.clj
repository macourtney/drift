(ns config.migrate-config
  (:require [drift.builder :as builder]))

(def version (atom nil))
(def init-run? (atom false)) 

(defn memory-current-version []
  (or @version 0)) 

(defn memory-update-version [new-version]
  (swap! version #(identity %2) new-version)) 

(defn init [args]
  (compare-and-set! init-run? false true)) 

(defn migrate-config []
  { :directory "/test/migrations"
    :current-version memory-current-version
    :update-version memory-update-version
    :init init
    :ns-content "\n  (:use clojure.contrib.sql)"
    :migration-number-generator builder/incremental-migration-number-generator })