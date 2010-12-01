(ns poc.canvas
  "Scrollable canvas for displaying the image."
  (:use (poc swt utils image)
	(little-gui-helper properties)))

(import-swt)

(defonce *image* (atom nil))

(add-watch *data* :recreate-image
	   (fn [_ _ _ [_ preview-data _]]
	     (swap! *image* (fn [current new]
			      (dispose-safely current)
			      (if new (Image. (default-display) new)))
		    preview-data)))

(defn bounds [w]
  (let [b (.getBounds w)] [(.width b) (.height b)]))

(defn repaint [gc canvas image [x y]]
  (if (every? ok? [gc canvas image])
    (let [[cw ch] (bounds canvas)
	  [iw ih] (bounds image)]
      (.drawImage gc image x y)
      (doseq [[a b c d] [[0        0        x           ch]
			 [x        0        iw          y]
			 [x        (+ y ih) iw          (- ch y ih)]
			 [(+ x iw) 0        (- cw x iw) ch]]]
	(.fillRectangle gc a b c d)))
    (if (every? ok? [gc canvas])
      (.fillRectangle gc (.getBounds canvas)))))

(defonce *scroll-delta* (atom [0 0 ]))

(let [previous-point (atom nil)]
  (defn scroll
    "Scroll canvas without flicker."
    [event canvas]
    (if (not= 0 (bit-and (.stateMask event) SWT/BUTTON1))
      (let [x (.x event), y (.y event)]
	(if-let [[prev-x prev-y] @previous-point]
	  (let [scroll-x (- x prev-x)
		scroll-y (- y prev-y)
		[cw ch] (bounds canvas)]
	    (.scroll canvas scroll-x scroll-y 0 0 cw ch false)
	    (swap! *scroll-delta* (fn [[act-x act-y]]
				    [(+ act-x scroll-x)
				     (+ act-y scroll-y)]))))
	(reset! previous-point [x y]))
      (reset! previous-point nil))))
      
    
(defn canvas [parent]
  (let [canvas (Canvas. parent SWT/NO_BACKGROUND)]
    (doprops canvas
	     :+paint.paint-control
	     (repaint (.gc event) canvas @*image* @*scroll-delta*)
	     :+mouse-move.mouse-move
	     (scroll event canvas))
    (add-watch *image* :repain-canvas
	       (fn [& _] (async-exec #(.redraw canvas))))
    canvas))