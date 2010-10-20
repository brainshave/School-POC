(ns poc.image
  (:import (org.eclipse.swt.widgets Display)
	   (org.eclipse.swt.graphics Image ImageData)))


(def *image* (atom nil))
(def *scroll-delta* (atom [0 0]))

(def *original-data* (agent nil))

(def *image-data* (agent nil))

(add-watch *original-data* :clone-image
	   (fn [_ _ _ new-data]
	     (if new-data
	       (send *image-data* (fn [_] (.clone new-data))))))


(add-watch *image-data*	:image-reloader
	   (fn [_ _ _ new-data]
	     (when new-data
	       (swap! *image* (fn [current-image]
				(if current-image
				  (.dispose current-image))
				(if new-data
				  (Image. (Display/getDefault)
					  new-data)
				  nil)))
	       (swap! *scroll-delta* (fn [_] [0 0])))))
	   
	   
(defn open-file [file-name]
  (send *original-data* (fn [_] (ImageData. file-name))))


(defn realign-image [canvas image]
  (if image
    (swap! *scroll-delta*
	   (fn [[x y]]
	     (let [canvas-area (.getClientArea canvas)
		   image-area (.getBounds image)]
	       (map int
		    [(max x (/ (- (.width canvas-area)
				  (.width image-area))
			       2))
		     (max y (/ (- (.height canvas-area)
				  (.height image-area))
			       2))]))))))