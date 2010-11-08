(ns poc.image
  (:import (org.eclipse.swt.widgets Display MessageBox)
	   (org.eclipse.swt.graphics Image ImageData PaletteData ImageLoader)
	   (org.eclipse.swt SWT)
	   (poc ByteWorker)))

(def ^{:doc "Original image data loaded from file."}
     *original-data* (agent nil))

(def ^{:doc "Original histograms"}
     *original-histograms* (agent ^{:size 1} [(int-array 256)
					      (int-array 256)
					      (int-array 256)
					      (int-array 256)]))

(def ^{:doc "A map where key is priority and value is a fn.  Fn takes
  four arguments: First is dereffered value of atom which was added to
  *transformations*, rest is: reds, greens, blues. Each one is a
  sequence of consecutive mappings for color: first element is mapping
  for pixel of value 0, and so on"}
     *transformations* (atom {}))

(def ^{:private true :doc "Value from every recalc-color-mapping will start"}
     *color-mappings-start* (->> (range 256) (repeat 3) vec))

(def ^{:doc "An agent, where *transformations* are applied. Value
  kept its a vector of final color mappings: [reds, greens, blues]"}
     *color-mappings* (agent *color-mappings-start*))

(defn ok? [image]
  (and image (not (.isDisposed image))))

(defn dispose-safely [image]
  (.asyncExec (Display/getDefault)
	      #(if (ok? image)
		 (.dispose image))))

(defn to-byte-array
  "Convert sequence to byte array"
  [seq]
  (->> seq
       (map #(.intValue %))
       (map #(cond
	      (> % 255) -1
	      (> % 127) (- % 256)
	      (< % 0) 0
	      true %))
       (map byte)
       (into-array Byte/TYPE)))

(def ^{:doc "*color-mappings* converted to byte arrays"}
     *color-byte-mappings* (atom (map to-byte-array @*color-mappings*)))

(def ^{:doc "Manipulated image data. Mapping color mappings to byte
array of image will happen here."}
     *image-data* (agent nil))

(add-watch *original-data* :clone-image
	   (fn [_ _ _ new-data]
	     (when new-data
	       (send *image-data* (fn [_] (.clone new-data)))
	       (send *color-mappings* identity)))) ;; to apply transformations on new image

(defn calc-histograms
  [[r g b rgb] data]
  (ByteWorker/calcHistograms data r g b rgb)
  (let [size (* (.width data)
		(.height data))]
    ^{:size size} [r g b rgb]))

(add-watch *original-data* :calc-original-histograms
	   (fn [_ _ _ new-data]
	     (when new-data
	       (send *original-histograms* calc-histograms new-data))))
					   

(add-watch *color-mappings* :convert-to-bytes
	   (fn [_ _ _ mappings]
	     (reset! *color-byte-mappings* (map to-byte-array mappings))))

(defn apply-color-mappings [image-data mappings]
  (comment (print "Start do-color-mapping, size:"
		  (count (.data image-data)) "... "))
  (let [original-data @*original-data*
	data image-data
	[reds greens blues] mappings]
    (if (and original-data image-data)
      (ByteWorker/applyMaps original-data data
			    reds greens blues))
    image-data))

(add-watch *color-byte-mappings* :apply-transforms
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
				  (dispose-safely current-image)
				  
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


(def ^{:doc "Plot image data and image"}
     *plot-data* (agent (let [data (ImageData. 256 256 24
					       (PaletteData. 0xff0000 0xff00 0xff))
			      image nil]
			  [data image])))

(add-watch *color-byte-mappings* :plot-plot
	   (fn [_ _ _ [reds greens blues]]
	     (send *plot-data* (fn [[data image]]
				 (ByteWorker/plotMaps data
						      reds greens blues)
				 (let [new-image (Image. (Display/getDefault)
							 data)]
				   (dispose-safely image)
				   [data new-image])))))
				  
(def *original-histogram-meta* (atom {:r? true :g? true :b? true :rgb? true :scale 28}))

(def *original-histogram-data*
     (agent (let [data (ImageData. 256 128 24
				   (PaletteData. 0xff0000 0xff00 0xff))
		  image nil]
	      [data image])))

(def *final-histograms*  (agent ^{:size 1} [(int-array 256)
					    (int-array 256)
					    (int-array 256)
					    (int-array 256)]))
(add-watch *image-data* :calc-final-histograms
	   (fn [_ _ _ new-data]
	     (when new-data
	       (send *final-histograms* calc-histograms new-data))))

(def *final-histogram-data*
     (agent (let [data (ImageData. 256 128 24
				   (PaletteData. 0xff0000 0xff00 0xff))
		  image nil]
	      [data image])))

(defn plot-histogram
  [[data image] [r g b rgb :as hists] {:keys [r? g? b? rgb? scale]}]
  (let [size (/ (-> hists meta :size) scale)]
    (ByteWorker/plotHistograms data (int size)
			       (if r? r) (if g? g) (if b? b) (if rgb? rgb)))
  (let [new-image (Image. (Display/getDefault) data)]
    (if (ok? image) (.dispose image))
    [data new-image]))

(add-watch *original-histograms* :plot-histograms
	   (fn [_ _ _ hists]
	     (send *original-histogram-data*
		   plot-histogram hists @*original-histogram-meta*)))

(add-watch *original-histogram-meta* :plot-histograms
	   (fn [_ _ _ hist-meta]
	     (send *original-histogram-data*
		   plot-histogram @*original-histograms* hist-meta)
	     (send *final-histogram-data*
		   plot-histogram @*final-histograms* hist-meta)))

(add-watch *final-histograms* :plot-histograms
	   (fn [_ _ _ hists]
	     (send *final-histogram-data*
		   plot-histogram hists @*original-histogram-meta*)))


(defn open-file [file-name]
  (println "Otwieram" file-name)
  (send *original-data*
	(fn [_] (let [data (ImageData. file-name)]
		  (if (.. data palette isDirect)
		    data
		    (.asyncExec (Display/getDefault)
				#(-> (Display/getDefault)
				     .getShells
				     first
				     (MessageBox. (reduce bit-or [SWT/ICON_ERROR, SWT/OK]))
				     (doto
					 (.setText "Błąd: Indeskowany obrazek")
				       (.setMessage "Program nie działa na indeksowanych obrazkach.")
				       (.open)))))))))

(defn save-file [file-name]
  (println "Zapisuję" file-name)
  (when-let [data @*image-data*]
    (let [loader (ImageLoader.)]
      (set! (.data loader) (into-array [data]))
      (.save loader file-name SWT/IMAGE_PNG))))
    

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
