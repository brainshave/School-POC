(ns poc.tools.convolution
  (:use (little-gui-helper properties)
	(clojure pprint)
	(poc tools swt))
  (:import (poc Convolution)))

(import-swt)

(def convkey "Splot")
(def minkey "Minimum")
(def maxkey "Maximum")
(def medkey "Mediana")

(defn convolution [{:keys [matrix algorithm]} data-in data-out]
  (try 
    (condp = algorithm
	convkey (Convolution/filter data-in data-out matrix)
	minkey (Convolution/minimum data-in data-out matrix)
	maxkey (Convolution/maximum data-in data-out matrix)
	medkey (Convolution/median data-in data-out matrix))
    (catch Exception e (.printStackTrace e))))

(defprotocol IGrid
  (matrix [grid])
  (dispose [grid]))

(defrecord Grid
  [inputs width height]
  IGrid
  (matrix [grid] (let [array (make-array Integer/TYPE
					 (count inputs)
					 (count (first inputs)))]
		   (dorun
		    (->> inputs
			 (map-indexed
			  (fn [col-num col]
			    (dorun (->> col
					(map-indexed
					 (fn [row-num input]
					   (aset array col-num row-num
						 (Integer/parseInt (.getText input)))))))))))
		   array))
  (dispose [grid] (doseq [col inputs, x col] (.dispose x))))
		   
					
(defn create-grid [panel old-grid +cols +rows]
  ;; get matrix earlier and apply from the center of new one
  (if old-grid (dispose old-grid))
  (let [{:keys [width height]} (if old-grid old-grid {:width 0 :height 0})
	new-width (max 1 (+ width +cols))
	new-height (max 1 (+ height +rows))
	inputs (doall
		(for [col (range new-width)]
		  (doall
		   (for [row (range new-height)]
		     (doprops (Text. panel (bit-or SWT/SINGLE 0))
			      :text "1"
			      :layout-data (format "width 25::,cell %d %d" col row))))))]
    (doto panel .layout .pack)
    (Grid. inputs new-width new-height)))

(defn grid-control-button [parent grid-atom button +cols +rows]
  (doprops button
	   :text (let [num (if (not= 0 +cols) +cols +rows)]
		   (if (< 0 num) (str "+" num) (str num)))
	   :+selection.widget-selected
	   (reset! grid-atom (create-grid parent @grid-atom +cols +rows))))

(defn mode-radio-button [button text vs-atom]
  (doprops button
	   :text text
	   :+selection.widget-selected
	   (swap! vs-atom #(assoc % :algorithm text))))

(defrecord MatrixTool
  [name vs panel grid]
  ITool
  (reset [tool])
  (parameters [tool] vs)
  (function [tool] convolution)
  (create-panel [tool parent]
		(let [new-panel (Composite. parent SWT/NONE)
		      layout (MigLayout. "wrap 3"
					 "[fill,grow][fill,grow][fill]"
					 "[fill,grow][fill,grow][fill]")
		      grid-scroll (ScrolledComposite. new-panel (bit-or SWT/V_SCROLL SWT/H_SCROLL))
		      grid-container (Composite. grid-scroll SWT/NONE)
		      grid-layout (MigLayout.)
		      new-grid (create-grid grid-container nil 3 3)
		      add-col (Button. new-panel SWT/PUSH)
		      del-col (Button. new-panel SWT/PUSH)
		      add-row (Button. new-panel SWT/PUSH)
		      del-row (Button. new-panel SWT/PUSH)
		      go (Button. new-panel SWT/PUSH)
		      normal-radio (Button. new-panel SWT/RADIO)
		      median-radio (Button. new-panel SWT/RADIO)
		      min-radio (Button. new-panel SWT/RADIO)
		      max-radio (Button. new-panel SWT/RADIO)]
		  (doprops grid-scroll
			   :layout-data "span 2 2, height 200!"
			   :content grid-container)
		  (doprops grid-container :layout grid-layout)
		  (.pack grid-container)
		  (doseq [[button +cols +rows] [[add-col 2 0]
						[del-col -2 0]
						[add-row 0 2]
						[del-row 0 -2]]]
		    (grid-control-button grid-container grid button +cols +rows))
		  (doprops go
			   :layout-data "span 1 3"
			   :text "!"
			   :+selection.widget-selected
			   (swap! vs #(assoc % :matrix (matrix @grid))))
		  (doseq [[radio text] [[normal-radio convkey]
					[median-radio medkey]
					[min-radio minkey]
					[max-radio maxkey]]]
		    (mode-radio-button radio text vs))
		  (doprops normal-radio :selection true)
		  (doprops new-panel :layout layout)
		  (reset! panel new-panel)
		  (reset! grid new-grid)
		  new-panel)))

(add-tool 51 (MatrixTool. "Splot" (atom {}) (atom nil) (atom nil)))