(ns poc.image
  (:use (poc workers swt)))

(import-swt)

(defonce ^{:doc "Holds three ImageData objects in a vector.

  First is original image data with eventually applied changes (after
  applying, changes are unrevertable), second is buffer meant to be
  displayed on screen (has applied preview transformations), third is
  a temporary buffer used when applying transformations."}
     *data* (worker [nil nil nil]))

(defn open-file [f]
  (let [data (ImageData. f)]
    (if (.. data palette isDirect)
      (let [clone1 (future (.clone data))
	    clone2 (future (.clone data))] 
	(send-task *data* (fn [_] [data @clone1 @clone2])))
      (message "Błąd: Indeskowany obrazek"
	       "Program nie działa na indeksowanych obrazkach."))))

