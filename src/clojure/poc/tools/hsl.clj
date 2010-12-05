(ns poc.tools.hsl
  (:use (poc tools utils))
  (:import (poc HSL ColorModelAdjustment)))

(let [model (HSL.)]
  (defn hsl [{h "H" s "S" l "L"} data-in data-out]
    (ColorModelAdjustment/adjust data-in data-out model (int-array [h s l]))))
  ;;(array-copy (.data data-in) (.data data-out)))

(add-tool 32 (tool "HSL"
		   hsl
		   ["H" 0 -180 180]
		   ["S" 0 -255 255]
		   ["L" 0 -255 255]))
		   