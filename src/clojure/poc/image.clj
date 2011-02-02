(ns poc.image
  (:use (poc workers swt utils))
  (:import poc.Compression))

(import-swt)

(defonce ^{:doc "Holds three ImageData objects in a vector.

  First is original image data with eventually applied changes (after
  applying, changes are unrevertable), second is buffer meant to be
  displayed on screen (has applied preview transformations), third is
  a temporary buffer used when applying transformations."}
     *data* (worker [nil nil nil nil]))

(def empty-candies {:fns [] :watched []})

(defonce ^{:doc "A map. Operations that operate on previews are kept
  under :fns and watched refs are kept under :watched."}
  *candies* (atom empty-candies))


(defn open-file [f]
  (let [data 
	(if (.endsWith (.toLowerCase f) ".swftw")
	  (Compression/uncompress f)
	  (ImageData. f))]
    (if (.. data palette isDirect)
      (let [clone1 (future (.clone data))
	    clone2 (future (.clone data))
	    clone3 (future (.clone data))] 
	(send-task *data* (fn [_] [data @clone1 @clone2 @clone3])))
      (message "Błąd: Indeskowany obrazek"
	       "Program nie działa na indeksowanych obrazkach."))))

(defn save-swftw [f quality]
  (Compression/compress (second @*data*) f quality))

(defn save-png [f]
  (let [data (second @*data*)
	loader (ImageLoader.)]
    (set! (.data loader) (into-array [data]))
    (.save loader f SWT/IMAGE_PNG)))

(defn run-operations
  "Runs through all :fns in *candies* on *data*.

  Fns are called with three arguments: current value of an atom
  associated with operation (with add-operation), data-in,
  data-out (both of type org.eclipse.swt.graphics.ImageData). First
  invocation uses orignal data as data-in and preview-data as
  data-out. Next invocation uses preview-data as data-in and tmp as
  data-out. From this point next invocations use interchangeably pairs
  of [preview tmp] and [tmp preview] as [data-in data-out].

  Returned value is [original last-used-as-data-out the-other buffer]."
  [[original preview tmp spare]]
  ;;(array-copy (.data original) (.data tmp))
  (let [{:keys [fns watched]} @*candies*
	circle (cycle (list preview tmp))
	inputs (cons original circle)
	outputs circle]
    (dorun (map #(try (%1 @%2 %3 %4)
		      (catch IllegalArgumentException e
			(%1 @%2 %3 %4 spare)))
		fns watched inputs outputs))
    (if (-> fns count even?)
      [original tmp preview spare]
      [original preview tmp spare])))
	 

(defn add-operation
  "Adds operation f to operations and starts watching a. Any change to
  a will fire run-operations automatically.

  f should be a function of value of a, data-in, data-out."
  [f a]
  (swap! *candies* (fn [candies]
		     (assoc candies
		       :fns (conj (:fns candies) f)
		       :watched (conj (:watched candies) a))))
  (add-watch a :run-operations
	     (fn [& _] (send-task *data* run-operations))))

(defn cancel-changes
  "Unregisters watches on all :watched from *candies* and clears the
  *candies* vectors. Returns data as-is."
  ([] (dorun (map #(remove-watch % :run-operations)
		  (:watched @*candies*)))
     (reset! *candies* empty-candies))
  ([[original preview tmp spare]]
     (cancel-changes)
     (array-copy (.data original) (.data preview))
     [original preview tmp spare]))

  
(defn apply-changes
  "Does what cancel-changes but swaps preview with base images (so new
  base image is that one with applied all changes)."
  [[original preview tmp spare]]
  (cancel-changes)
  (array-copy (.data preview) (.data original))
  [original preview tmp spare])
