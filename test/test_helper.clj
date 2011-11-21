(ns test-helper
  (:import [java.io File]
           [org.apache.log4j ConsoleAppender Level Logger PatternLayout]
           [org.apache.log4j.varia LevelRangeFilter])
  (:use clojure.test))

(def output-pattern (new PatternLayout "%-5p [%c]: %m%n"))

(def console-appender (new ConsoleAppender output-pattern))
(.addFilter console-appender 
  (doto (new LevelRangeFilter)
    (.setLevelMin Level/WARN)))

(doto (Logger/getRootLogger)
  (.setLevel Level/ALL)
  (.addAppender console-appender))

(defn 
#^{:doc "Verifies the given file is not nil, is an instance of File, and has the given name."}
  test-file [file expected-file-name]
  (is file)
  (is (instance? File file))
  (when file
    (is (= expected-file-name (.getName file)))))
  
(defn
#^{:doc "Simply calls test-file on the given directory and name."}
  test-directory [directory expected-directory-name]
  (test-file directory expected-directory-name))