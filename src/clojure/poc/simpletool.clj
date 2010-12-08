(ns poc.simpletool
  (:use (little-gui-helper properties)
	(poc tools swt)))

(import-swt)

(defprotocol ISliderRow
  (reset-row [row spec] "Take slider-spec and reset this row appriopriately."))

(defrecord SliderRow [label display slider]
  ISliderRow
  (reset-row [row {:keys [value f]}]
	     (async-exec #(do (doprops slider :selection value)
			      (doprops display :text (str (f value)))))))

(defprotocol ISliderSpec
  (create-row [spec parent atom] "Create a row with label, value display and slider."))

(defrecord SliderSpec [name value min max f]
  ISliderSpec
  (create-row [spec parent atom]
	      (let [label (Label. parent SWT/HORIZONTAL)
		    display (Label. parent SWT/HORIZONTAL)
		    slider (Scale. parent SWT/HORIZONTAL)]
		(doprops label :text name)
		(doprops display :text (str (f value)))
		(doprops slider
			 :minimum min
			 :maximum max
			 :selection value
			 :+selection.widget-selected
			 (let [v (f (.getSelection slider))]
			   (doprops display :text (str v))
			   (swap! atom #(assoc %1 name %2) v)))
		(SliderRow. label display slider))))
	      
(defrecord SimpleTool
  ;;"Simple tool consisting only of simple sliders."
  [name f init vs slider-specs panel slider-rows]
  ITool
  (create-panel [tool parent]
		(let [new-panel (Composite. parent SWT/NONE)
		      layout (MigLayout. "wrap 3" "[right][fill,30!][fill,grow]")
		      sliders (doall (map #(create-row % new-panel vs)
					  slider-specs))]
		  (doprops new-panel :layout layout)
		  (reset! panel new-panel)
		  (reset! slider-rows sliders)
		  new-panel))
  (reset [tool]
	 (dorun (map reset-row @slider-rows slider-specs))
	 (reset! vs init))
  (parameters [tool] vs)
  (function [tool] f))
	 
(defn normalize-slider
  "Normalizes slider, so min is 0 (in SWT sliders can't have negative values).
  Eventually leaves calculation of value in map for user if f is
  provided. If f is provided does nothing."
  [[name value min max f]]
  (if-not f
    (SliderSpec. name (- value min) 0 (- max min) #(+ % min))
    (SliderSpec. name value min max f)))

(defn simple-tool
  "Define a tool. f is a function that applies tranformation on
  ImageData. Takes three arguments

  slider-spec: [name, value, min, max, f?] (Provide f when conversion from
  slider value to value in map is non-linear)"
  [name f & sliders]
  (let [normalized-sliders (map normalize-slider sliders)
	initial-values (reduce (fn [map {:keys [name value f]}]
				 (assoc map name (f value)))
			       {} normalized-sliders)]
    (SimpleTool. name f
		 initial-values (atom initial-values)
		 normalized-sliders (atom nil) (atom nil))))
