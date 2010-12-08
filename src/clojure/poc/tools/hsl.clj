(ns poc.tools.hsl
  (:use (poc tools simpletool utils))
  (:import (poc HSL ColorModelAdjustment)))

(let [model (HSL.)
      adjuster (ColorModelAdjustment.)]
  (defn hsl [{h "H" s "S" l "L"} data-in data-out]
    (.adjust adjuster data-in data-out model (int-array [h s l]))))

(add-tool 32 (simple-tool "HSL"
			  hsl
			  ["H" 0 -180 180]
			  ["S" 0 -255 255]
			  ["L" 0 -255 255]))
