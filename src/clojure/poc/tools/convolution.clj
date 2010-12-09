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
	new-width (max 1 (+ width +cols))
	new-height (max 1 (+ height +rows))
	inputs (doall (for [col (range new-width)]
			(doall (for [row (range new-height)]
				 (doprops (Text. panel SWT/SINGLE)
					  :text "0"
					  :layout-data (format "width 25::,cell %d %d" col row))))))]
    (.pack panel)
    (Grid. inputs new-width new-height)))

(defn grid-control-button [parent grid-atom button +cols +rows]
  (doprops button
	   :text (let [num (if (not= 0 +cols) +cols +rows)]
		   (if (< 0 num) (str "+" num) (str num)))
	   :+selection.widget-selected
	   (reset! grid-atom (create-grid parent @grid-atom +cols +rows))))

(defn table [parent width height]
  (let [table (Table. parent (reduce bit-or [SWT/SINGLE]))
	table-editor (TableEditor. table)]
    (doprops table :lines-visible true
	     :+selection.widget-selected
	     (do (if-let [old (.getEditor table-editor)]
		   (.dispose old))
		 (if-let [item (.item event)]
		   (let [new-editor (Text. table SWT/NONE)]
		     (doprops new-editor
			      :text (.getText item 0)
			      :+modify.modify-text
			      (.setText item 0 (.getText new-editor)))
		     (doto new-editor
		       (.selectAll)
		       (.setFocus))))))
    (dotimes [i 10]
      (doprops (TableColumn. table SWT/NONE)
	       :text (str i)))
    (dotimes [r 128]
      (let [row (TableItem. table SWT/NONE)]
	(dotimes [i 10]
	  (doprops row
		   :text ^unroll (i (str (* i r)))))))
    (dotimes [i 10]
      (.. table (getColumn i) pack))
    table))

(defrecord MatrixTool
  [name vs panel grid]
  ITool
  (reset [tool] (reset! vs nil))
  (parameters [tool] vs)
  (function [tool] convolution)
  (create-panel [tool parent]
		(let [new-panel (Composite. parent SWT/NONE)
		      layout (MigLayout. "wrap 3" "[fill,grow][fill,grow][fill]"
					 "[fill,grow][fill,grow][fill]")
		      grid-scroll (ScrolledComposite. new-panel (bit-or SWT/V_SCROLL SWT/H_SCROLL))
		      grid-container (Composite. grid-scroll SWT/NONE)
		      grid-layout (MigLayout. "" "[fill,grow]")
		      new-grid (create-grid grid-container nil 3 3)
		      ;;new-grid (table new-panel 3 3)
		      add-col (Button. new-panel SWT/PUSH)
		      del-col (Button. new-panel SWT/PUSH)
		      add-row (Button. new-panel SWT/PUSH)
		      del-row (Button. new-panel SWT/PUSH)
		      go (Button. new-panel SWT/PUSH)]
		  (doprops grid-scroll
			   ;;:background (Color. (default-display) 0 0 0)
			   :layout-data "span 2 2,width 30::, height 200!")
		  (doprops grid-container :layout grid-layout)
		  (.pack grid-container)
			   ;;:size ^unroll (500 500))
		  (doprops grid-scroll :content grid-container)
		  ;(.pack grid-container)
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
		  (doprops new-panel :layout layout)
		  (reset! panel new-panel)
		  (reset! grid new-grid)
		  new-panel)))

(add-tool 51 (MatrixTool. "Splot" (atom nil) (atom nil) (atom nil)))