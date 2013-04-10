(ns drift.config
  (:require [clojure.string :as string]))

(declare find-migrate-dir-name outer-dir-in-path find-config missing-param)

(def config-ns-symbol 'config.migrate-config)

(def accessors
  {'current-version-fn :current-version
   'default-ns-content :ns-content
   'find-init-fn :init
   'find-migrate-dir-name :directory
   'find-src-dir :src
   'migration-namespaces :migration-namespaces
   'migration-number-generator :migration-number-generator
   'namespace-prefix :namespace-prefix
   'update-version-fn :update-version})

(def defaults
  {:directory (constantly "/src/migrate")
   :src #(-> % find-migrate-dir-name outer-dir-in-path)})

(def required-params
  #{:current-version :update-version})

(defn- get-param
  ([name] (get-param name (find-config)))
  ([name config]
   (or (get config name)
       (when-let [default-fn (defaults name)] (default-fn config))
       (if (contains? required-params name)
         (missing-param name)))))

(doseq [[fn-name param-name] (seq accessors)]
  (intern *ns* fn-name #(apply get-param param-name %&)))

(defn find-config-namespace []
  (require config-ns-symbol)
  (find-ns config-ns-symbol))

(defn find-config []
  (when-let [migrate-config-namespace (find-config-namespace)]
    (when-let [migrate-config-fn (ns-resolve migrate-config-namespace 'migrate-config)]
      (migrate-config-fn))))

(defn- missing-param [param]
  (throw (java.lang.NullPointerException.
           (str "Missing configuration parameter in migrate-config: " param))))

(def ^:private separators #{"/" "\\"})

(defn outer-dir-in-path [path]
  (let
    [separator-positions
     (filter (complement neg?) (map #(.indexOf path % 1) separators))]
    (when-let
      [first-separator-position
       (if-not (empty? separator-positions) (apply min separator-positions))]
      (.substring path 0 (inc first-separator-position)))))
