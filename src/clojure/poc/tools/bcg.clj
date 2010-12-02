(ns poc.tools.bcg
  (:use (poc tools utils))
  (:import (poc ByteWorker)))

(defonce bkey "Jasność")
(defonce ckey "Kontrast")
(defonce gkey "Gamma")

(defn calc-maps [brightness contrast gamma]
  (map #(-> %
	    ;; gamma: 
	    (/ 256) double (Math/pow (/ 1 gamma)) (* 256)
	    ;; contrast:
	    (- 128) (* (Math/tan (* 1/2 Math/PI
				    (/ (+ contrast 128) 256))))
	    (+ 128)
	    ;; brightness
	    (+ brightness)
	    (try (catch ArithmeticException e 255)))
       (range 256)))

  
(defn bcg [{brightness bkey, contrast ckey, gamma gkey} data-in data-out]
  (let [m (to-byte-array (calc-maps brightness contrast gamma))]
    (ByteWorker/applyMaps data-in data-out m m m)))

(add-tool 1 (tool "Jasność, kontrast, gamma"
		  bcg
		  [bkey 0 -255 255]
		  [ckey 0 -255 255]
		  [gkey 100 1 190  #(float (if (<= % 100)
						(/ % 100)
						(+ (/ (- % 100) 10) 1)))]))
