(ns poc.plots)

(def *height* 128)

(defonce ^{:doc "Backing data for plots. A map containing [f data]'s
  where f is function of image-data and old-backing-data that
  returns new backing-data, which is stored in as data.

  Data is recalculated when poc.image/*data* changes."}
  *backing-data* (atom {}))

(defonce ^{:doc "Plots sorted by priority in wich are displayed."}
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
