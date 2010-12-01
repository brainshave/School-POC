(ns poc.tools.bcg
  (:use (poc tools)))

(add-tool 1 (tool "Jasność, kontrast, gamma"
		identity
		["Jasność" 0 -255 255]
		["Kontrast" 0 -255 255]
		["Gamma" 100 1 190  #(float (if (<= % 100)
					      (/ % 100)
					      (+ (/ (- % 100) 10) 1)))]))