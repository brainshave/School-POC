(ns poc.tools.luv
  (:use (poc tools utils))
  (:import (poc Luv ColorModelAdjustment)))

(let [model (Luv.)
      adjuster (ColorModelAdjustment.)]
  (defn luv [{l "L" u "u" v "v"} data-in data-out]
    (.adjust adjuster data-in data-out model (int-array [l u v]))))

(add-tool 42 (tool "Luv"
		   luv
		   ["L" 0 -100 100]
		   ["u" 0 -127 127]
		   ["v" 0 -127 127]))
