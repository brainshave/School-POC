(ns poc.tools
  (:use (poc utils image workers)))

;;(import-swt)

(defonce ^{:doc "Tools sorted by priority"}
  *tools* (atom (sorted-map)))

(defprotocol ITool
  "An abstract tool."
  (reset [tool] "Reset tool to initial state. ")
  (create-panel [tool parent] "Generate panel for this tool.")
  (parameters [tool] "Return atom that holds value of tool parameters.")
  (function [tool] "Returns function that transforms image."))


(defn add-tool
  "Adds abstract tool to *tools*."
  [priority tool]
  (swap! *tools* #(assoc %1 priority tool)))

(defn reset-tool
  "Takes tool structure and tool-panel structure and resets its to defaults"
  [tool]
  (remove-all-watchers (parameters tool))
  (reset tool)
  (add-watch (parameters tool) :first-change
	     (fn [k a _ _]
	       (remove-watch a k) ;; add operation only once
	       (add-operation (function tool) a) ;; a will be watched
	       (send-task *data* run-operations)))) ;; force first calculation

(defn reset-tools []
  (dorun (map reset-tool (vals @*tools*))))

(defn tool-panels
  "Generates new panels for all *tools*.
  Returns sequence of pairs [tool-name panel]"
  [parent]
  (let [names-panels
	(doall (map (fn [tool]
		      [(:name tool) (create-panel tool parent)])
		    (vals @*tools*)))]
    (reset-tools)
    names-panels))

