(ns poc.image
  (:import (org.eclipse.swt.widgets Display)
	   (org.eclipse.swt.graphics Image ImageData)
	   (poc ByteWorker)))


(def *image* (atom nil))
(def *scroll-delta* (atom [0 0]))

(def *original-data* (agent nil))

(def *image-data* (agent nil))

(add-watch *original-data* :clone-image
	   (fn [_ _ _ new-data]
	     (if new-data
	       (send *image-data* (fn [_] (.clone new-data))))))

;; refresh images when transform finishes
(add-watch *image-data*	:image-refresher
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

(defn apply-transform [transform & args]
  (apply send *image-data* transform args))

(defn do-color-mapping [image-data mapping]
  (println "Start do-color-mapping, size:" (count (.data image-data)))
  (let [original-data (.data @*original-data*)
	data (.data image-data)]
    (ByteWorker/work original-data data mapping)
    ;; 	n (count data)]
    ;; (loop [i 0]
    ;;   (when (< i n)
    ;; 	(let [old-byte (aget original-data i)]
    ;; 	  (aset-byte data i (aget mapping (if (< old-byte 0)
    ;; 					    (+ 256 old-byte)
    ;; 					    old-byte))))
    ;; 	(recur (inc i))))
    (println "End do-color-mapping.")
    image-data))

(def *brightness-contrast-gamma*
     (atom {:brightness 0
	    :contrast 0
	    :gamma 1.0}))

(defn new-color-mapping [brightness contrast gamma]
  (into-array Byte/TYPE
	      (map byte (for [color (range 256)]
			  (let [new-color (+ color brightness)]
			    (cond
			     (> new-color 255) -1
			     (> new-color 127) (- new-color 256)
			     (< new-color 0) 0
			     true new-color))))))
    

(defn apply-color-mapping [mapping]
  (apply-transform do-color-mapping mapping))


(add-watch *brightness-contrast-gamma* :modify
	   (fn [_ _ _ {:keys [brightness contrast gamma]}]
	     (apply-color-mapping (new-color-mapping brightness contrast gamma))))

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