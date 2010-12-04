(ns poc.tools.histogram
  (:use poc.swt
	little-gui-helper.properties)
  (:require (poc [image :as image]
		 [transformations :as transformations])))

(import-swt)

(defn widget [parent]
  (let [panel (Composite. parent SWT/NONE)
	layout (MigLayout. "fill" "[right, grow][][][][left, grow][]")
	input-histogram (Canvas. panel SWT/NO_BACKGROUND)
	scale (doprops (Scale. panel SWT/VERTICAL)
			     :layout-data "height 128!, wrap"
			     :maximum 127
			     :minimum 0
			     :selection 100)
	show-label (doprops (Label. panel SWT/HORIZONTAL)
				  :text "Wyświetl:")
	show-buttons (doall (map #(let [button (Button. panel SWT/TOGGLE)]
				    (doprops
				     button
				     :text %1
				     :selection true
				     :+selection.widget-selected
				     (swap! image/*original-histogram-meta*
					    (fn [old]
					      (assoc old %2
						     (.getSelection button))))))
				 ["R" "G" "B" "RGB"]
				 [:r? :g? :b? :rgb?]))
	empty1 (doprops (Label. panel SWT/HORIZONTAL)
			      :layout-data "wrap")
	balance-label (doprops (Label. panel SWT/HORIZONTAL)
				     :text "Wyrównaj:")
	balance-buttons (doall (map #(let [button (Button. panel SWT/TOGGLE)]
				       (doprops
					button
					:text %1
					:selection false
					:+selection.widget-selected
					(swap! transformations/*balance-histograms*
					       (fn [old]
						 (assoc old %2
							(.getSelection button))))))
				    ["R" "G" "B" "RGB"]
				    [:r? :g? :b? :rgb?]))
	empty2 (doprops (Label. panel SWT/HORIZONTAL)
			      :layout-data "wrap")
	output-histogram (Canvas. panel SWT/NO_BACKGROUND)]
    (.setEnabled (last balance-buttons) false) ;; TODO: RGB balancing
    (doprops input-histogram
		   :layout-data "span 5, center, width 256!, height 128!"
		   :+paint.paint-control
		   (let [image (-> @image/*original-histogram-data* second)]
		     (if (image/ok? image)
		       (.. event gc (drawImage image 0 0))
		       (doto (.. event gc)
			 ;; TODO: black bg
			 (.fillRectangle 0 0 (.. input-histogram getBounds width)
					 (.. input-histogram getBounds height))))))
    (doprops output-histogram
		   :layout-data "span 5, center, width 256!, height 128!"
		   :+paint.paint-control
		   (let [image (-> @image/*final-histogram-data* second)]
		     (if (image/ok? image)
		       (.. event gc (drawImage image 0 0))
		       (doto (.. event gc)
			 ;; TODO: black bg
			 (.fillRectangle 0 0 (.. output-histogram getBounds width)
					 (.. output-histogram getBounds height))))))
    (doprops scale
		   :+selection.widget-selected
		   (swap! image/*original-histogram-meta*
			  #(assoc % :scale (- 128 (.getSelection scale)))))
    (add-watch image/*original-histogram-data* :draw-histogram
	       (fn [_ _ _ _]
		 (.asyncExec (Display/getDefault) #(if (image/ok? input-histogram)
						     (.redraw input-histogram)))))
    (add-watch image/*final-histogram-data* :draw-histogram
	       (fn [_ _ _ _]
		 (.asyncExec (Display/getDefault) #(if (image/ok? output-histogram)
						     (.redraw output-histogram)))))
    (doprops panel :layout layout)))
