(ns poc.tools.cmyk
  (:use (poc tools))
  (:import (poc ByteWorker)))

(defn cmyk [{c "C" m "M" y "Y" k "K"} data-in data-out]
  (ByteWorker/cmykCorrection data-in data-out c m y k))


(add-tool 31 (tool "CMYK"
		   cmyk
		   ["C" 0 -255 255]
		   ["M" 0 -255 255]
		   ["Y" 0 -255 255]
		   ["K" 0 -255 255]))
		   