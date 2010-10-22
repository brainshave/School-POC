(ns poc.image
  (:import (org.eclipse.swt.widgets Display)
	   (org.eclipse.swt.graphics Image ImageData)
	   (poc ByteWorker)))

(def ^{:doc "Original image data loaded from file."}
     *original-data* (agent nil))

(def ^{:doc "A map where key is priority and value is a fn.  Fn takes
  three arguments: reds, greens, blues. Each one is a sequence of
  consecutive mappings for color: first element is mapping for pixel
  of value 0, and so on"}
     *transformations* (atom {}))

(def ^{:private true :doc "Value from every recalc-color-mapping will start"}
     *color-mappings-start* (->> (range 256) (repeat 3) vec))

(def ^{:doc "An agent, where *transformations* are applied. Value
  kept its a vector of final color mappings: [reds, greens, blues]"}
     *color-mappings* (agent *color-mappings-start*))


(def ^{:doc "Manipulated image data. Mapping color mappings to byte
array of image will happen here."}
     *image-data* (agent nil))

(add-watch *original-data* :clone-image
	   (fn [_ _ _ new-data]
	     (when new-data
	       (send *image-data* (fn [_] (.clone new-data)))
	       (send *color-mappings* identity)))) ;; to apply transformations on new image

(defn to-byte-array
  "Convert sequence to byte array"
  [seq]
  (->> seq
       (map #(cond
	      (> % 255) -1
	      (> % 127) (- % 256)
	      (< % 0) 0
	      true %))
       (map byte)
       (into-array Byte/TYPE))) 

(defn apply-color-mappings [image-data mappings]
  (comment (print "Start do-color-mapping, size:"
		  (count (.data image-data)) "... "))
  (let [original-data (.data @*original-data*)
	data (.data image-data)
	[reds greens blues] (map to-byte-array mappings)]
    (ByteWorker/work original-data data
			   reds greens blues)
    image-data))

(add-watch *color-mappings* :apply-transforms
	   (fn [_ _ _ color-mappings]
	     (send *image-data* apply-color-mappings color-mappings)))

(def *image* (atom nil))

;; refresh images when transform finishes
(add-watch *image-data*	:image-refresher
	   (fn [_ _ _ new-data]
	     (when new-data
	       (swap! *image* (fn [current-image]
				(let [new-image (if new-data
						  (Image. (Display/getDefault)
							  new-data))]
				  (if current-image
				    (.dispose current-image))
				  
				  new-image))))))

(defn recalc-color-mappings
  "Reduce *color-mappings* by applying consecutive tranformations from
  *transformations*. Transformations are ordered by key."
  [_]
  (->> @*transformations* (sort-by first)
       (reduce (fn [[reds greens blues] [_ [f a]]]
		 (f @a reds greens blues))
	       *color-mappings-start*)))
  

(defn add-transformation
  "Add transformation f which depends on atom a with priority prio.
  f must be a function of atom_value, reds, greens, blues.
  See documentation of *transformations*."
  [prio f a]
  (swap! *transformations*  #(assoc % prio [f a]))
  (add-watch a :transfrom-watcher
	     (fn [_ _ _ _]
	       (send *color-mappings* recalc-color-mappings))))


;; old



;; VARIOUS TRANSFORMATIONS



  
;; (defn apply-transform [transform & args]
;;   (apply send *image-data* transform args))
;;  
;; (defn new-color-mapping [brightness contrast gamma]
;;   (into-array Byte/TYPE
;; 	      (map byte (for [color (range 256)]
;; 			  (let [new-color (-> color
;; 					      (/ 256) double (Math/pow (/ 1 gamma)) (* 256) ;; gamma
;; 					      (* (+ 1 (/ contrast 128))) (- contrast) ;; contrast
;; 					      (+ brightness))]
;; 			    (cond
;; 			     (> new-color 255) -1
;; 			     (> new-color 127) (- new-color 256)
;; 			     (< new-color 0) 0
;; 			     true new-color))))))
    

;; (defn apply-color-mapping [mapping]
;;   (apply-transform do-color-mapping mapping))


;; (add-watch *brightness-contrast-gamma* :modify
;; 	   (fn [_ _ _ {:keys [brightness contrast gamma]}]
;; 	     (apply-color-mapping (new-color-mapping brightness contrast gamma))))

(defn open-file [file-name]
  (println "Otwieram" file-name)
  (send *original-data* (fn [_] (ImageData. file-name))))


(def *scroll-delta* (atom [0 0]))

(defn realign-image [canvas image-data]
  (if image-data
    (.asyncExec (Display/getDefault)
		#(reset! *scroll-delta*
			 (let [canvas-area (.getClientArea canvas)]
			   (map int
				[(max 0 (/ (- (.width canvas-area)
					      (.width image-data))
					   2))
				 (max 0 (/ (- (.height canvas-area)
					      (.height image-data))
					   2))]))))))
