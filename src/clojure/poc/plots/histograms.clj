(ns poc.plots.histograms
  (:use (poc plots))
  (:import (poc ByteWorker)
	   (poc.plots Plot)))

(defn calc-histograms [data [r g b rgb]]
  (ByteWorker/calcHistograms data r g b rgb)
  [r g b rgb])

(defn histogram-with [name & colors]
  (let [colors (into #{} colors)]
    (Plot. name :histograms 256
	   (fn [plot-image-data [r g b rgb]]
	     (ByteWorker/plotHistograms plot-image-data
					100
					(if (colors :r) r)
					(if (colors :g) g)
					(if (colors :b) b)
					(if (colors :rgb) rgb))))))

(add-backing-data :histograms calc-histograms (repeatedly 4 #(int-array 256)))

(doseq [[priority color] (partition 2 [21 :rgb 22 :r 23 :g 24 :b])]
  (add-plot priority (histogram-with (name color) color)))
(add-plot 25 (histogram-with "rgb" :r :g :b :rgb))



