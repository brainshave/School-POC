(ns poc.ui-elements
  (:require (little-gui-helper [properties :as props])
	    (poc [image :as image]
		 [transformations :as transformations]
		 [whole-image :as whole])
	    (poc.tools [histogram :as histogram]))
  (:use poc.swt))

(import-swt)

  

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
	(histogram/widget expand-bar)
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
	
	"Podgląd korekcji kolorów" (make-bcg-plot expand-bar)

	"CMYK"
	(let [panel (Composite. expand-bar SWT/NONE)
	      layout (MigLayout. "" "[center][center, fill, grow][center][fill, 30!]")
	      make-row (fn [panel text-left text-right key]
			 (let [label-left (props/doprops (Label. panel SWT/HORIZONTAL)
							 :text text-left)
			       scale (Scale. panel SWT/HORIZONTAL)
			       label-right (props/doprops (Label. panel SWT/HORIZONTAL)
							  :text text-right)
			       label-counter (props/doprops (Label. panel SWT/HORIZONTAL)
							    :layout-data "wrap"
							    :text "0")]
			   (props/doprops scale
					  :minimum 0
					  :maximum 510
					  :selection 255
					  :+selection.widget-selected
					  (let [v (- (.getSelection scale)
						     255)]
					    (.setText label-counter (str v))
					    (swap! whole/cmyk-control
						   #(assoc % key v))))))]
	  (doall (map #(apply make-row panel %)
		      [["R" "C" :c] ["G" "M" :m] ["B" "Y" :y] ["W" "K" :k]]))
	  (props/doprops panel :layout layout))))
    

(defn make-expand-bar [shell]
  (let [scroll (ScrolledComposite. shell (bit-or SWT/BORDER SWT/V_SCROLL))
	expand-bar (ExpandBar. scroll SWT/V_SCROLL)]
    (doseq [[label panel] (partition 2 (make-tools expand-bar))]
      (let [expand-item (ExpandItem. expand-bar SWT/NONE)]
	(props/doprops expand-item
		       :text label
		       :control panel
		       :expanded false
		       :height (-> panel (.computeSize SWT/DEFAULT SWT/DEFAULT) .y))))
    (props/doprops expand-bar nil)
    (props/doprops scroll
		   :content expand-bar
		   :expand-horizontal true
		   :expand-vertical true
		   :min-size (.computeSize expand-bar SWT/DEFAULT SWT/DEFAULT))))

		   
	