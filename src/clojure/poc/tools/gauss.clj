(ns poc.tools.gauss
  (:use (poc tools simpletool))
  (:import (poc Convolution)))

(def xkey "Szerokość")
(def ykey "Wysokość")

(defn gauss [x s]
  (/ (Math/exp (- (/ (* x x) (* 2 s s))))
     (Math/sqrt (* 2 Math/PI)) s))

(defn gauss-1-dim [width]
  {:pre [(odd? width)
	 (pos? width)]}
  (let [s (/ width 6)]
    (map #(gauss % s)
	 (range (- (int (/ width 2))) (inc (int (/ width 2)))))))

(defn gauss-2-dim [width height]
  (let [xs (gauss-1-dim width)
	ys (gauss-1-dim height)]
    (for [x xs] (for [y ys] (* x y)))))

(defn normalize-1d [top col]
  (let [sum (reduce + col)]
    (for [x col] (int (* (/ x sum) top)))))

(defn normalize-2d [top col]
  (let [sum (reduce + (flatten col))]
    (for [row col]
      (for [x row] (int (* (/ x sum) top))))))

(defn int-2d-array [col]
  (into-array (map #(into-array Integer/TYPE %) col)))

(defn gauss-convolve [{width xkey, height ykey} data-in data-out]
  (Convolution/filter data-in data-out
		      (int-2d-array
		       (normalize-2d
			(bit-shift-left 1 16)
			(gauss-2-dim width height)))))

(defn gauss-convolve-opt [{width xkey, height ykey} data-in data-out spare]
  (let [top (bit-shift-left 1 16)
	xs (int-2d-array
	    (map list (normalize-1d top (gauss-1-dim width))))
	ys (int-2d-array
	    (list (normalize-1d top (gauss-1-dim height))))]
    (Convolution/filter data-in spare xs)
    (Convolution/filter spare data-out ys)))

(add-tool 61 (simple-tool "Gauss"
			  gauss-convolve-opt
			  [xkey 0 0 40 #(inc (* 2 %))]
			  [ykey 0 0 40 #(inc (* 2 %))]))
 