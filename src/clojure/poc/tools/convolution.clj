(ns poc.tools.convolution
  (:use (little-gui-helper properties)
	(clojure pprint)
	(poc tools swt fftw))
  (:import (poc Convolution DoubleWorker)
	   com.schwebke.jfftw3.JFFTW3))

(import-swt)

(def convkey "Splot")
(def fftwkey "Splot FFT")
(def minkey "Minimum")
(def maxkey "Maximum")
(def medkey "Mediana")


;; filtrowanie:
;; wrzucam do maski takiego samego rozmiaru jak obraz na sam środek maskę (cz. rzeczywistą)
;; dla każdego koloru i dla maski robisz Forward:
;; dla każej liczby z tablic wyjściowych (R, I) każdego koloru:
;; Rm, Im - odpowiednia liczba zesp. z maski
;; R := R * Rm - I * Im
;; I := I * Rm + R * Im
;; każdy kolor <- Backward
;; normalizacja każdego koloru (?)

(defn fill-real-matrix [width height matrix buff]
  (let [matrix-width (count matrix)
	matrix-height (-> matrix first count)
	top-margin (int (/ (- height matrix-height) 2))
	bottom-margin (int (- height top-margin matrix-height))
	left-margin (int (/ (- width matrix-width) 2))
	right-margin (int (- width matrix-width left-margin))
	fill-zeros #(dotimes [_ (* % 2)] (.put buff 0.0))]
  (.rewind buff)
  ;;(time (fill-zeros (* top-margin width)))
  (let [asdf (* 2 top-margin width)]
  (time (dotimes [_ asdf]
	  (.put buff 0.0))))
  (dotimes [x matrix-height]
    (fill-zeros left-margin)
    (dotimes [y matrix-width]
      (doto buff
	(.put (double (aget matrix x y)))
	(.put 0.0)))
    (fill-zeros right-margin))
  (time (fill-zeros (* bottom-margin width)))))

(defn fftw-convolution [matrix data-in data-out]
  (let [width (.width data-in)
	height (.height data-in)
	alloc #(malloc (* width height))
	matrix-width (count matrix)
	matrix-height (-> matrix first count)]
    (with-fftw [mat-ptr (alloc)
		mat-forward (forward-plan width height mat-ptr)]
      (let [mat (JFFTW3/jfftw_complex_get mat-ptr)]
	;;(time (fill-real-matrix width height matrix mat))
	(.rewind mat)
	(dotimes [y matrix-height]
	  (dotimes [x matrix-width]
	    (doto mat
	      (.put (double (aget matrix x y)))
	      (.put 0.0)))
	  (dotimes [_ (* 2 (- width matrix-width))]
	    (.put mat 0.0)))
	(dotimes [_ (.remaining mat)]
	  (.put mat 0.0))
	(.rewind mat)
	(JFFTW3/jfftw_execute mat-forward)
	(dotimes [diff 3]
	  (with-fftw [color-ptr (alloc)
		      forward (forward-plan width height color-ptr)
		      backward (backward-plan width height color-ptr)]
	    (let [color (JFFTW3/jfftw_complex_get color-ptr)]
	      (DoubleWorker/fillComplexColor diff data-in color)
	      (JFFTW3/jfftw_execute forward)
	      (.rewind color)
	      (.rewind mat)
	      (dotimes [_ (* width height)]
		(.mark color)
		(let [Rm (.get mat)
		      Im (.get mat)
		      R  (.get color)
		      I  (.get color)]
		  (doto color
		    (.reset) ; back to marked pos
		    ;; R := R * Rm - I * Im
		    ;; I := I * Rm + R * Im
		    (.put (- (* R Rm) (* I Im)))
		    (.put (+ (* I Rm) (* R Im))))))
	      (JFFTW3/jfftw_execute backward)
	      ;; (.rewind color)
	      ;; (println (reduce min (for [i (range 0 (.limit color))]
	      ;; 			     (.get color))))
	      ;; (.rewind color)
	      ;; (println (reduce max (for [i (range 0 (.limit color))]
	      ;; (.get color))))
	      (.rewind color)
	      (DoubleWorker/renderColor diff color data-out))))))))
	      
(defn convolution [{:keys [matrix algorithm]} data-in data-out]
  (try 
    (condp = algorithm
	convkey (Convolution/filter data-in data-out matrix)
	fftwkey (fftw-convolution matrix data-in data-out)
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
		      fftw-radio (Button. new-panel SWT/RADIO)
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
					[fftw-radio fftwkey]
					[median-radio medkey]
					[min-radio minkey]
					[max-radio maxkey]]]
		    (mode-radio-button radio text vs))
		  (doprops normal-radio :selection true)
		  (doprops new-panel :layout layout)
		  (reset! panel new-panel)
		  (reset! grid new-grid)
		  new-panel)))

(add-tool 51 (MatrixTool. "Splot"
			  (atom {:algorithm convkey
				 :matrix (into-array (repeat 3 (int-array '(1 1 1))))})
			  (atom nil) (atom nil)))