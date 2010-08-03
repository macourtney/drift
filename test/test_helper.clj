(ns test-helper
  (:import [java.io File])
  (:use clojure.contrib.test-is))
  
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