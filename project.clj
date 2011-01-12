(defproject poc "0.3.0-SNAPSHOT"
  :description "Project for Digital Image Processing course (Przetwarzanie Obrazów Cyfrowych in Polish)"
  :dependencies [[org.clojure/clojure "1.2.0"]
		 [org.clojure/clojure-contrib "1.2.0"]
		 [little-gui-helper "0.1.0-SNAPSHOT"]
		 [com.miglayout/miglayout "3.7.3.1" :classifier "swt"]
		 ;; added manually using maven:
		 [orphant/swt "3.6.1"]
		 [orphant/jfftw3 "0.1"]
		 [orphant/gluegen-rt "1.0"]] 
  :dev-dependencies [[swank-clojure "1.2.1"]]
  :clean-non-project-classes true
  :main poc.core
  :source-path "src/clojure"
  :java-source-path "src/java"
  :jvm-opts ["-Xmx1g" "-Xms400m"])
