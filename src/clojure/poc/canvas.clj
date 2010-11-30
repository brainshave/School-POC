(ns poc.canvas
  "Scrollable canvas for displaying the image."
  (:use (poc swt utils image)
	(little-gui-helper properties)))

(import-swt)

(def *image* (atom nil))

(def *scroll-delta* (atom [0 0]))

(defn repaint [gc canvas image [x y]]
  (if (every? ok? [gc canvas image])
    (let [get-bounds #(let [b (.getBounds %)] [(.width b) (.height b)])
	  [cw ch] (get-bounds canvas)
	  [iw ih] (get-bounds image)]
      (.drawImage gc x y)
      (doseq [[a b c d] [[0        0        x           ch]
			 [x        0        iw          y]
			 [x        (+ y ih) iw          (- ch y ih)]
			 [(+ x iw) 0        (- cw x iw) ch]]]
	(.fillRectangle gc a b c d)))
    (if (every? ok? [gc canvas])
      (.fillRectangle gc (.getBounds canvas)))))
	    
    
(defn canvas [parent]
  (let [canvas (Canvas. parent SWT/NO_BACKGROUND)]
    (doprops canvas
	     :+paint.paint-control
	     (repaint (.gc event) canvas @*image* @*scroll-delta*))
    canvas))