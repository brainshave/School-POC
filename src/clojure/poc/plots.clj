(ns poc.plots
  (:use (poc swt image)
	(little-gui-helper properties)))

(import-swt)

(def *height* 128)

(defonce ^{:doc "Backing data for plots. A map containing [f data]'s
  where f is function of image-data and old-backing-data that
  returns new backing-data, which is stored in as data.

  Data is recalculated when poc.image/*data* changes."}
  *backing-data* (atom {}))

(defonce ^{:doc "Plots sorted by priority in wich are displayed. Value
is [plot plot-data canvas]."}
  *plots* (atom (sorted-map)))

(defrecord ^{:doc "Specification of a plot. Plots are redrawed using
  each time *backing-data* is changed. f takes two arguments:
  image-data to draw this plot on and data stored in *backing-data*
  for approriate key."}
  Plot [name key width f])

(defn recalc-backing-data [image-data]
  (swap! *backing-data*
	 (fn [backing-data]
	   (into {} (doall (pmap (fn [[key [f data]]]
				   [key [f (f image-data data)]])
				 backing-data))))))

(defn add-backing-data [key f data]
  (swap! *backing-data* #(assoc % key [f data])))

(defn default-palette [] (PaletteData. 0xff0000 0xff00 0xff))

(defn add-plot [priority plot]
  (let [init-data (ImageData. (:width plot) *height* 24 (default-palette))]
    (swap! *plots* #(assoc % priority [plot init-data nil]))))

(defn plot-canvas
  "Returns new Canvas object that always simply paints
  data (ImageData) on itself."
  [parent plot data]
  (let [canvas (Canvas. parent SWT/NO_BACKGROUND)
	image (atom nil)]
    (doprops canvas
	     :layout-data (format "width %d!, height %d!" (:width plot) *height*)
	     :+paint.paint-control
	     (do (dispose-safely @image)
		 (reset! image (Image. (default-display) data))
		 (.drawImage (.gc event) @image 0 0)))))

(defn plot-canvases
  "Creates canvases for plots."
  [parent]
  (reset! *plots*
	  (reduce (fn [plots [priority [plot data]]]
		    (assoc-in plots [priority 2] (plot-canvas parent plot data)))
		  @*plots* @*plots*)))

(defn redraw-plots []
  (dorun (pmap (fn [[{:keys [key f]} data canvas]]
		 (f data (-> @*backing-data* key second))
		 (async-exec #(.redraw canvas)))
	       (vals @*plots*))))

(add-watch *data* :redraw-plots
	   (fn [_ _ _ [_ data _]]
	     (future
	      (recalc-backing-data data)
	      (redraw-plots))))
		 

