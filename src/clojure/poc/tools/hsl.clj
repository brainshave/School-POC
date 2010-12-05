(ns poc.tools.hsl
  (:use (poc tools))
  (:import (poc HSL)))

(defn hsl [{h "H" s "S" l "L"} data-in data-out]
  )

(add-tool 32 (tool "HSL"
		   hsl
		   ["H" 0 -180 180]
		   ["S" 0 -127 127]
		   ["L" 0 -127 127]))
		   