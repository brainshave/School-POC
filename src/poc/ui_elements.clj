(ns poc.ui-elements
  (:require (little-gui-helper [properties :as props])
	    (poc [image :as image]
		 [transformations :as transformations]))
  (:import (org.eclipse.swt.widgets Menu MenuItem
				    FileDialog ExpandBar ExpandItem
				    Composite Label Scale Canvas Display
				    Button)
	   (org.eclipse.swt.custom ScrolledComposite)
	   (org.eclipse.swt SWT)
	   (org.eclipse.swt.events SelectionListener PaintListener)
	   (net.miginfocom.swt MigLayout)))


  

(defn make-menu-bar [shell canvas]
  (let [menu-bar (Menu. shell SWT/BAR)
	file-item (props/doprops (MenuItem. menu-bar SWT/CASCADE)
				 :text "&Plik")
	file-menu (Menu. shell SWT/DROP_DOWN)
	open-dialog (props/doprops (FileDialog. shell SWT/OPEN)
				   :filter-extensions
				   (into-array ["*.jpg;*.png;*.bmp"]))
	open-item (props/doprops (MenuItem. file-menu SWT/PUSH)
				 :text "&Otwórz...\tCtrl+O"
				 :accelerator (+ SWT/MOD1 (int \O))
				 :+selection.widget-selected
				 (when-let [file-name (.open open-dialog)]
				   (image/open-file file-name)))
	save-dialog (props/doprops (FileDialog. shell SWT/SAVE)
				   :filter-extensions
				   (into-array ["*.png"]))
	save-item (props/doprops (MenuItem. file-menu SWT/PUSH)
				 :text "&Zapisz jako PNG...\tCtrl+S"
				 :accelerator (+ SWT/MOD1 (int \S))
				 :+selection.widget-selected
				 (when-let [file-name (.open save-dialog)]
				   (image/save-file file-name)))
	separator1 (MenuItem. file-menu SWT/SEPARATOR)
	exit-item (props/doprops (MenuItem. file-menu SWT/PUSH)
				 :text "&Wyjdź\tCtrl+Q"
				 :accelerator (+ SWT/MOD1 (int \Q))
				 :+selection.widget-selected (.close shell))]
    (props/doprops file-item
		   :menu file-menu)
    menu-bar))

(defmacro setup-scale [scale display key min max selection formula]
  `(props/doprops ~scale
		  :minimum ~min
		  :maximum ~max
		  :selection ~selection
		  :layout-data "wrap"
		  :+selection.widget-selected
		  (let [~(symbol "selection") (.getSelection ~scale)
			value# ~formula]
		    (.setText ~display
			      (str value#))
		    (swap! transformations/*brightness-contrast-gamma*
			   (fn [act-val#]
			     (assoc act-val# ~key value#))))))

(defn make-bcg-plot [parent]
  (let [panel (Composite. parent SWT/NONE)
	plot (Canvas. panel SWT/NO_BACKGROUND)]
    (props/doprops panel
		   :layout (MigLayout. "fill"))
    (props/doprops plot
		   :size ^unroll (256 256)
		   :background (-> (Display/getDefault) (.getSystemColor SWT/COLOR_LIST_BACKGROUND))
		   :layout-data "center, width 256!, height 256!"
		   :+paint.paint-control
		   (let [image (-> @image/*plot-data* second)]
		     (if (image/ok? image)
		       (.. event gc (drawImage image 0 0)))))
    (add-watch image/*plot-data* :plot-on-canvas
	       (fn [_ _ _ _]
		 (.asyncExec (Display/getDefault) #(if (image/ok? plot)
						     (.redraw plot)))))
    panel))

(defn- make-tools [expand-bar]
  (list "Histogramy: wejściowy i wyjściowy"
	(let [panel (Composite. expand-bar SWT/NONE)
	      layout (MigLayout. "fill" "[right, grow][][][][left, grow][]")
	      input-histogram (Canvas. panel SWT/NO_BACKGROUND)
	      scale (props/doprops (Scale. panel SWT/VERTICAL)
				   :layout-data "height 128!, wrap"
				   :maximum 127
				   :minimum 0
				   :selection 100)
	      show-label (props/doprops (Label. panel SWT/HORIZONTAL)
					:text "Wyświetl:")
	      show-buttons (doall (map #(let [button (Button. panel SWT/TOGGLE)]
				     (props/doprops
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
	      empty1 (props/doprops (Label. panel SWT/HORIZONTAL)
				   :layout-data "wrap")
	      balance-label (props/doprops (Label. panel SWT/HORIZONTAL)
					   :text "Wyrównaj:")
	      balance-buttons (doall (map #(let [button (Button. panel SWT/TOGGLE)]
					     (props/doprops
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
	      empty2 (props/doprops (Label. panel SWT/HORIZONTAL)
				    :layout-data "wrap")
	      output-histogram (Canvas. panel SWT/NO_BACKGROUND)]
	  (.setEnabled (last balance-buttons) false) ;; TODO: RGB balancing
	  (props/doprops input-histogram
			 :layout-data "span 5, center, width 256!, height 128!"
			 :+paint.paint-control
			 (let [image (-> @image/*original-histogram-data* second)]
			   (if (image/ok? image)
			     (.. event gc (drawImage image 0 0))
			     (doto (.. event gc)
			       ;; TODO: black bg
			       (.fillRectangle 0 0 (.. input-histogram getBounds width)
					       (.. input-histogram getBounds height))))))
	  (props/doprops output-histogram
			 :layout-data "span 5, center, width 256!, height 128!"
			 :+paint.paint-control
			 (let [image (-> @image/*final-histogram-data* second)]
			   (if (image/ok? image)
			     (.. event gc (drawImage image 0 0))
			     (doto (.. event gc)
			       ;; TODO: black bg
			       (.fillRectangle 0 0 (.. output-histogram getBounds width)
					       (.. output-histogram getBounds height))))))
	  (props/doprops scale
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
	  (props/doprops panel :layout layout))

	"Poziomki"
	(let [panel (Composite. expand-bar SWT/NONE)
	      layout (MigLayout. "" "[fill,grow]")
	      make-controls (fn [panel label-text start-key end-key]
			      (let [label (props/doprops (Label. panel SWT/HORIZONTAL)
							 :text (str label-text "0-255")
							 :layout-data "wrap")
				    start-scale (Scale. panel SWT/HORIZONTAL)
				    end-scale (Scale. panel SWT/HORIZONTAL)]
				(props/doprops start-scale
					       :minimum 0 :maximum 255
					       :selection 0
					       :+selection.widget-selected
					       (.setMinimum end-scale
							    (.getSelection start-scale)))
				(props/doprops end-scale
					       :minimum 0 :maximum 255
					       :selection 255
					       :+selection.widget-selected
					       (.setMaximum start-scale
							    (.getSelection end-scale)))
				(doseq [scale [start-scale end-scale]]
				  (props/doprops scale
						 :layout-data "wrap"
						 :+selection.widget-selected
						 (do (.setText label
							       (str label-text
								    (.getSelection start-scale)
								    "-"
								    (.getSelection end-scale)))
						     (swap! transformations/*levels*
							    #(assoc % start-key
								    (.getSelection start-scale)
								    end-key
								    (.getSelection end-scale))))))
				[label start-scale end-scale]))
	      ins (make-controls panel "Wejściowe: " :in-start :in-end)
	      outs (make-controls panel "Wyjściowe: " :out-start :out-end)]
	  (props/doprops panel :layout layout))

	"Balans kolorów"
	(let [panel (Composite. expand-bar SWT/NONE)
	      layout (MigLayout. "" "[right][center, fill, grow][][]")
	      make-row (fn [panel text-left text-right key]
			 (let [label-left (props/doprops (Label. panel SWT/HORIZONTAL)
							 :text text-left)
			       scale (Scale. panel SWT/HORIZONTAL)
			       label-right (props/doprops (Label. panel SWT/HORIZONTAL)
							  :text text-right)
			       label-counter (props/doprops (Label. panel SWT/HORIZONTAL)
							    :layout-data "wrap"
							    :text "____")]))]
	  (doall (map #(apply make-row panel %)
		      [["R" "C" :c] ["G" "M" :m] ["B" "Y" :y] ["W" "K" :k]]))
	  (props/doprops panel :layout layout))
	      
	
	"Jasność, kontrast, gamma"
	(let [panel (Composite. expand-bar SWT/NONE)
	      layout (MigLayout. "" "[right][fill,30!][left,fill,grow]")
	      brightness-label (props/doprops (Label. panel SWT/HORIZONTAL)
					      :text "Jasność:")
	      brightness-display (props/doprops (Label. panel SWT/HORIZONTAL)
						:text "0")
	      brightness-scale (Scale. panel SWT/HORIZONTAL)
	      contrast-1-label (props/doprops (Label. panel SWT/HORIZONTAL)
					      :text "Kontrast:")
	      contrast-1-display (props/doprops (Label. panel SWT/HORIZONTAL)
						:text "0")
	      contrast-1-scale (Scale. panel SWT/HORIZONTAL)
	      gamma-label (props/doprops (Label. panel SWT/HORIZONTAL)
					 :text "Gamma:")
	      gamma-display (props/doprops (Label. panel SWT/HORIZONTAL)
					   :text "1.0")
	      gamma-scale (Scale. panel SWT/HORIZONTAL)]
	  (setup-scale brightness-scale brightness-display :brightness
		       0 512 256 (- selection 256))
	  (setup-scale contrast-1-scale contrast-1-display :contrast
		       0 256 128 (- selection 128))
	  (setup-scale gamma-scale gamma-display :gamma
		       1 190 100 (float (if (<= selection 100)
					(/ selection 100)
					(+ (/ (- selection 100) 10) 1))))
	  (props/doprops panel :layout layout))
	
	"Podgląd korekcji kolorów" (make-bcg-plot expand-bar)))
    

(defn make-expand-bar [shell]
  (let [scroll (ScrolledComposite. shell (bit-or SWT/BORDER SWT/V_SCROLL))
	expand-bar (ExpandBar. scroll SWT/V_SCROLL)]
    (doseq [[label panel] (partition 2 (make-tools expand-bar))]
      (let [expand-item (ExpandItem. expand-bar SWT/NONE)]
	(props/doprops expand-item
		       :text label
		       :control panel
		       :expanded true
		       :height (-> panel (.computeSize SWT/DEFAULT SWT/DEFAULT) .y))))
    (props/doprops expand-bar nil)
    (props/doprops scroll
		   :content expand-bar
		   :expand-horizontal true
		   :expand-vertical true
		   :min-size (.computeSize expand-bar SWT/DEFAULT SWT/DEFAULT))))

		   
	