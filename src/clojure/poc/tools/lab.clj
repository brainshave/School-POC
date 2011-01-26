(ns poc.tools.lab
  (:use (poc tools simpletool utils))
  (:import (poc Lab ColorModelAdjustment)))

(let [model (Lab.)
      adjuster (ColorModelAdjustment.)]
  (defn lab [{l "L" a "a" b "b"} data-in data-out]
    (.adjust adjuster data-in data-out model (int-array [l a b]))))

(add-tool 41 (simple-tool "L*a*b"
			  lab
			  ["L" 0 -255 255]
			  ["a" 0 -255 255]
			  ["b" 0 -255 255]))
