(ns poc.tools.lab
  (:use (poc tools utils))
  (:import (poc Lab ColorModelAdjustment)))

(let [model (Lab.)
      adjuster (ColorModelAdjustment.)]
  (defn lab [{l "L" a "a" b "b"} data-in data-out]
    (.adjust adjuster data-in data-out model (int-array [l a b]))))

(add-tool 41 (tool "L*a*b"
		   lab
		   ["L" 0 -100 100]
		   ["a" 0 -127 127]
		   ["b" 0 -127 127]))
