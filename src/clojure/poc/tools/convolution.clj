(ns poc.tools.convolution
  (:use (little-gui-helper properties)
	(clojure pprint)
	(poc tools swt))
  (:import (poc Convolution)))

(import-swt)

(defn convolution [matrix data-in data-out]
  (Convolution/filter data-in data-out matrix))

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
  ;; get matrix earlier
  (if old-grid (dispose old-grid))
  (let [{:keys [width height]} (if old-grid old-grid {:width 0 :height 0})
	new-width (+ width +cols)
	new-height (+ height +cols)
	inputs (doall (for [col (range new-width)]
			(doall (for [row (range new-height)]
				 (doprops (Text. panel SWT/SINGLE)
					  :text "0"
					  :layout-data (format "cell %d %d" col row))))))]
    (.layout panel)
    (Grid. inputs new-width new-height)))

(defn grid-control-button [parent grid-atom button +cols +rows]
  (doprops button
	   :text (str (if (not= 0 +cols) +cols +rows))
	   :+selection.widget-selected
	   (reset! grid-atom (create-grid parent @grid-atom +cols +rows))))

(defrecord MatrixTool
  [name vs panel grid]
  ITool
  (reset [tool] (reset! vs nil))
  (parameters [tool] vs)
  (function [tool] convolution)
  (create-panel [tool parent]
		(let [new-panel (Composite. parent SWT/NONE)
		      layout (MigLayout. "wrap 3" "[fill,grow][fill,grow][fill]" "[fill,grow][fill,grow][fill]")
		      grid-container (Composite. new-panel SWT/NONE)
		      grid-layout (MigLayout.)
		      new-grid (create-grid grid-container nil 3 3)
		      add-col (Button. new-panel SWT/PUSH)
		      del-col (Button. new-panel SWT/PUSH)
		      del-row (Button. new-panel SWT/PUSH)
		      add-row (Button. new-panel SWT/PUSH)
		      go (Button. new-panel SWT/PUSH)]
		  (doprops grid-container
			   ;;:background (Color. (default-display) 0 0 0)
			   :layout-data "span 2 2"
			   :layout grid-layout)
		  (doseq [[button +cols +rows] [[add-col 2 0]
						[del-col -2 0]
						[add-row 0 2]
						[del-row 0 -2]]]
		    (grid-control-button grid-container grid button +cols +rows))
		  (doprops go
			   :+selection.widget-selected
			   (let [val (matrix @grid)]
			     (println "ASDFQWER")
			     (pprint val)
			     (reset! vs val)))
		  ;; (doprops add-col :text "+2")
		  ;; (doprops del-col :text "-2")
		  ;; (doprops add-row :text "+2")
		  ;; (doprops del-row :text "-2")
		  (doprops new-panel :layout layout)
		  (reset! panel new-panel)
		  (reset! grid new-grid)
		  new-panel)))

(add-tool 51 (MatrixTool. "Splot" (atom nil) (atom nil) (atom nil)))