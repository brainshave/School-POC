(ns poc.image
  (:use (poc workers swt tools)))

(import-swt)

(defonce ^{:doc "Holds three ImageData objects in a vector.

  First is original image data with eventually applied changes (after
  applying, changes are unrevertable), second is buffer meant to be
  displayed on screen (has applied preview transformations), third is
  a temporary buffer used when applying transformations."}
     *data* (worker [nil nil nil]))

(def empty-candies {:fns [] :watched []})

(defonce ^{:doc "A map. Operations that operate on previews are kept
  under :fns and watched refs are kept under :watched."}
  *candies* (atom empty-candies))


(defn open-file [f]
  (let [data (ImageData. f)]
    (if (.. data palette isDirect)
      (let [clone1 (future (.clone data))
	    clone2 (future (.clone data))] 
	(send-task *data* (fn [_] [data @clone1 @clone2])))
      (message "Błąd: Indeskowany obrazek"
	       "Program nie działa na indeksowanych obrazkach."))))

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
  [[original preview tmp]]
  (let [{:keys [fns watched]} @*candies*
	circle (cycle (list preview tmp))
	inputs (cons original circle)
	outputs circle]
    (dorun (map #(%1 @%2 %3 %4)
		fns watched inputs outputs))
    (if (-> fns count even?)
      [original tmp preview]
      [original preview tmp])))
	 

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
  ([data] (cancel-changes) data))

  
(defn apply-changes
  "Does what cancel-changes but swaps preview with base images (so new
  base image is that one with applied all changes)."
  [[original preview tmp]]
  (cancel-changes)
  [preview original tmp])
