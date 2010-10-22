(defproject poc "0.1.0-SNAPSHOT"
  :description "Project for Digital Image Processing course (Przetwarzanie Obrazów Cyfrowych in Polish)"
  :aot [#"poc\..*"]
  :main poc.poc
  :omit-source true
  :jvm-opts ["-Xmx1g"]
  :dependencies [[org.clojure/clojure "1.2.0"]
		 [org.clojure/clojure-contrib "1.2.0"]
		 [org.eclipse/swt-win32-win32-x86 "3.5.2"]
		 [little-gui-helper "0.1.0-SNAPSHOT"]
		 [com.miglayout/miglayout "3.7.3.1" :classifier "swt"]]
  :dev-dependencies [[swank-clojure "1.2.0"]])
