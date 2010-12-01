(ns poc.tools
  (:use (little-gui-helper properties)
	(poc swt utils)))

(import-swt)

(defonce ^{:doc "Tools sorted by priority"}
  *tools* (atom (sorted-map)))

(defn normalize-slider
  "Normalizes slider, so min is 0 (in SWT sliders can't have negative values).
  Eventually leaves calculation of value in map for user if f is
  provided. If f is provided does nothing."
  [[name value min max f]]
  (if-not f
    [name (- value min) 0 (- max min) #(+ % min)]
    [name value min max f]))

(defn tool
  "Define a tool. f is a function that applies tranformation on
  ImageData. Takes three arguments

  slider-spec: [name, value, min, max, f?] (Provide f when conversion from
  slider value to value in map is non-linear)"
  [name f & sliders]
  (let [normalized-sliders (map normalize-slider sliders)
	initial-values (reduce (fn [map [name value _ _ f]]
				 (assoc map name (f value)))
			       {} normalized-sliders)]
    {:name name
     :function f
     :init initial-values
     :values (atom initial-values)
     :slider-specs normalized-sliders}))

(defn slider-row
  "Generates row for slider with label and display of current value."
  [parent atom [name value min max f]]
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
    [label display slider]))
  
(defn tool-panel
  [parent {:keys [name function values slider-specs]}]
  (let [panel (Composite. parent SWT/NONE)
	layout (MigLayout. "wrap 3" "[right][fill,30!][fill,grow]")
	sliders (doall (map #(slider-row panel values %)
			    slider-specs))]
    (doprops panel :layout layout)
    {:panel panel :sliders sliders}))

(defn reset-slider [[_ display slider] [_ value _ _ f]]
  (doprops slider :selection value))
  ;; resetting display should will be triggered by selectionListerner

(defn reset-tool
  "Takes tool structure and tool-panel structure and resets its to defaults"
  [{:keys [slider-specs values function]}
   {:keys [sliders]}]
  (remove-all-watchers values)
  (dorun (map reset-slider slider-specs sliders))
  (add-watch values :first-change
	     (fn [k a _ _]
	       ;;(add-operation function a)
	       (println "First change")
	       (remove-watch a k)))) ;; add to *candies* only once

(defn add-tool
  "Adds abstract tool to *tools*."
  [priority tool]
  (swap! *tools* #(assoc %1 priority [tool nil])))

(defn tool-panels
  "Generates new panels for all *tools*.
  Returns sequence of pairs [tool-name panel]"
  [parent]
  (swap! *tools*
	 (fn [tools]
	   (reduce (fn [tools [prior [tool]]]
		     (assoc-in tools [prior 1] (tool-panel parent tool)))
		   tools tools)))
  (map (fn [[tool tool-panel]]
	 [(:name tool) (:panel tool-panel)])
       (vals @*tools*)))
  