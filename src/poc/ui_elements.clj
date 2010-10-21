(ns poc.ui-elements
  (:require (little-gui-helper [properties :as props])
	    (poc [image :as image]))
  (:import (org.eclipse.swt.widgets Menu MenuItem
				    FileDialog ExpandBar ExpandItem
				    Composite Label Scale Canvas Display)
	   (org.eclipse.swt.custom ScrolledComposite)
	   (org.eclipse.swt SWT)
	   (org.eclipse.swt.events SelectionListener)
	   (net.miginfocom.swt MigLayout)))


  

(defn make-menu-bar [shell canvas]
  (let [menu-bar (Menu. shell SWT/BAR)
	file-item (props/doprops (MenuItem. menu-bar SWT/CASCADE)
				 :text "&Plik")
	file-menu (Menu. shell SWT/DROP_DOWN)
	file-dialog (props/doprops (FileDialog. shell SWT/OPEN)
				   :filter-extensions
				   (into-array ["*.jpg;*.png;*.gif"]))
	open-item (props/doprops (MenuItem. file-menu SWT/PUSH)
				 :text "&Otwórz\tCtrl+O"
				 :accelerator (+ SWT/MOD1 (int \O))
				 :+selection.widget-selected
				 (when-let [file-name (-> file-dialog
							  .open)]
				   (println "Otwieram" file-name)
				   (image/open-file file-name)))
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
		    (swap! image/*brightness-contrast-gamma*
			   (fn [act-val#]
			     (assoc act-val# ~key value#))))))

(defn make-bcg-plot [parent]
  (let [plot (Canvas. parent SWT/BORDER)]
    (props/doprops plot
		   :size ^unroll (256 256)
		   :background (-> (Display/getDefault) (.getSystemColor SWT/COLOR_LIST_BACKGROUND))
		   :layout-data "center, span 3, width 256!, height 256!")))

(defn- make-tools [expand-bar]
  (list "Jasność, kontrast, nasycenie"
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
	      gamma-scale (Scale. panel SWT/HORIZONTAL)
	      plot (make-bcg-plot panel)]
	  (setup-scale brightness-scale brightness-display :brightness
		       0 512 256 (- selection 256))
	  (setup-scale contrast-1-scale contrast-1-display :contrast
		       0 256 128 (- selection 128))
	  (setup-scale gamma-scale gamma-display :gamma
		       1 190 100 (float (if (<= selection 100)
					(/ selection 100)
					(+ (/ (- selection 100) 10) 1))))
	  (props/doprops panel :layout layout))))
    

(defn make-expand-bar [shell]
  (let [scroll (ScrolledComposite. shell (bit-or SWT/BORDER SWT/V_SCROLL))
	expand-bar (ExpandBar. scroll SWT/V_SCROLL)]
    (doseq [[label panel] (partition 2 (make-tools expand-bar))]
      (let [expand-item (ExpandItem. expand-bar SWT/NONE)]
	(props/doprops expand-item
		       :text label
		       :control panel
		       :height (-> panel (.computeSize SWT/DEFAULT SWT/DEFAULT) .y))))
    (props/doprops expand-bar nil)
    (props/doprops scroll
		   :content expand-bar
		   :expand-horizontal true
		   :expand-vertical true
		   :min-size (.computeSize expand-bar SWT/DEFAULT SWT/DEFAULT))))

		   
	