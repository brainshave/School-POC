(ns poc.tools.unsharpmask
  (:use (poc tools simpletool)
	(poc.tools gauss))
  (:import (poc UnsharpMask)))

(def akey "Ilość")

(defn unsharp-mask [{width xkey, height ykey, amount akey} data-in data-out spare]
  (gauss-convolve-opt {xkey width, ykey height} data-in data-out spare)
  (UnsharpMask/filter data-in data-out data-out amount))

(add-tool 62 (simple-tool "Maska wyostrzająca"
			  unsharp-mask
			  [xkey 0 0 40 #(inc (* 2 %))]
			  [ykey 0 0 40 #(inc (* 2 %))]
			  [akey 30 0 60 #(double (- (/ % 10) 3))]))

