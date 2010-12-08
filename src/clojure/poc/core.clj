(ns poc.core
  (:gen-class)
  (:use (poc swt main-window
	     [workers :only [start-all-workers stop-all-workers]])))

(defn start []
  (async-exec main-window))


(defn -main [& args]
  (start-all-workers)
  (swt-loop (main-window))
  (stop-all-workers))

(defrecord Lalala [a b c d])