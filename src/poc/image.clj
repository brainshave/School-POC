(ns poc.image
  (:import (org.eclipse.swt.widgets Display)
	   (org.eclipse.swt.graphics Image)))


(def *image* (atom nil))
(def *scroll-delta* (atom [0 0]))

(defn open-file [file-name]
  (let [new-image (Image. (Display/getDefault)
			  file-name)]
    (swap! *image* (fn [current-image]
		     (if current-image
		       (.dispose current-image))
		     new-image))
    (swap! *scroll-delta* (fn [_] [0 0]))))

(defn realign-image [canvas]
  (if @*image*
    (swap! *scroll-delta*
	   (fn [[x y]]
	     (let [canvas-area (.getClientArea canvas)
		   image-area (.getBounds @*image*)]
	       (map int
		    [(max x (/ (- (.width canvas-area)
				  (.width image-area))
			       2))
		     (max y (/ (- (.height canvas-area)
				  (.height image-area))
			       2))]))))))