(defproject poc "0.1.0-SNAPSHOT"
  :description "Project for Digital Image Processing course (Przetwarzanie Obrazów Cyfrowych in Polish)"
  :repositories ["toochain.eu" "http://toolchain.eu/maven2/"]
  :dependencies [[org.clojure/clojure "1.2.0"]
		 [org.clojure/clojure-contrib "1.2.0"]
		 [little-gui-helper "0.1.0-SNAPSHOT"]
		 [org.eclipse.swt/swt-windows "3.6" :classifier "x86"]
		 [com.miglayout/miglayout "3.7.3.1" :classifier "swt"]]
  :dev-dependencies [[swank-clojure "1.2.1"]]
  :aot [#"poc\..*"]
  :main poc.core
  :source-path "src/clojure"
  :java-source-path "src/java"
  :jvm-opts ["-Xmx1g"])
