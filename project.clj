(defproject poc "0.1.0-SNAPSHOT"
  :description "Project for Digital Image Processing course (Przetwarzanie Obrazów Cyfrowych in Polish)"
  :aot [#"poc\..*"]
  :main poc.poc
  :dependencies [[org.clojure/clojure "1.2.0"]
		 [org.clojure/clojure-contrib "1.2.0"]
                 ;;[org.clojure.contrib/datalog "1.3.0-alpha1"]
		 [org.eclipse/swt-gtk-linux-x86 "3.5.2"]
		 [little-gui-helper "0.1.0-SNAPSHOT"]]
  :dev-dependencies [[swank-clojure "1.2.0"]])
