(ns poc.tools.histogram
  (:use (poc tools utils swt)
	(little-gui-helper properties))
  (:import poc.ByteWorker))

(import-swt)

(defn calc-balance-map [histogram factor]
  (to-byte-array
   (if histogram
     (let [cumulus (vec (reductions + histogram))]
       (map #(int (* factor (cumulus %1))) (range 256)))
     (range 256))))

(def straight-map (to-byte-array (range 256)))
  

(defn balance-histograms [{r "R" g "G" b "B" rgb "RGB"} data-in data-out]
  (let [rs (int-array 256)
	gs (int-array 256)
	bs (int-array 256)
	rgbs (int-array 256)
	size (* (.width data-in) (.height data-in))
	factor (/ 256 size)]
    (ByteWorker/calcHistograms data-in rs gs bs rgbs)
    (if rgb
      (let [m (calc-balance-map rgbs factor)]
	(ByteWorker/applyMaps data-in data-out m m m))
      (let [rmap (if r (calc-balance-map rs factor) straight-map)
	    gmap (if g (calc-balance-map gs factor) straight-map)
	    bmap (if b (calc-balance-map bs factor) straight-map)]
	(ByteWorker/applyMaps data-in data-out rmap gmap bmap)))))

(defrecord HistogramBalancer
  [name vs panel buttons]
  ITool
  (reset [tool]); (doall (map #(.setSelection % false) @buttons)))
  (parameters [tool] vs)
  (function [tool] balance-histograms)
  (create-panel [tool parent]
		(let [new-panel (Composite. parent SWT/NONE)
		      layout (MigLayout.)
		      new-buttons
		      (doall
		       (for [render-type ["R" "G" "B" "RGB"]]
			 (let [button (Button. new-panel SWT/CHECK)]
			   (doprops button
				    :text render-type
				    :+selection.widget-selected
				    (swap! vs #(assoc % render-type (.getSelection button)))))))]
		  (reset! panel new-panel)
		  (reset! buttons new-buttons)
		  (doprops new-panel :layout layout))))

(add-tool 21 (HistogramBalancer.
	      "Wyrównywanie histogramu" (atom {}) (atom nil) (atom nil)))