(ns poc.core
  (:gen-class)
  (:use (poc swt main-window)))

(defn start []
  (async-exec main-window))


(defn -main [& args]
  (swt-loop (main-window)))