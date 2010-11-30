(ns poc.image
  (:use (poc workers swt)))

(import-swt)

(def ^{:doc "Main image data, holds original image or image with applied
  transformations."}
     *data* (ref nil))

(def ^{:doc "Preview data, will be swapped with *data* when applying
  transformations."}
     *preview-data* (worker nil))

(defn open-file [f]
  (future (let [data (ImageData. f)]
	    (if (.. data palette isDirect)
	      (let [data-clone (.clone data)]
		(send-task @*data* (fn [_] data))
		(send-task @*preview-data* (fn [_] data-clone)))
	      (message "Błąd: Indeskowany obrazek"
		       "Program nie działa na indeksowanych obrazkach.")))))