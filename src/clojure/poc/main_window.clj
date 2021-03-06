(ns poc.main-window
  "Main window for this application."
  (:use (poc swt image tools plots
	     [canvas :only [canvas]]
	     [workers :only [send-task]])
	(little-gui-helper properties))
  ;; require something from poc.tools means adding it to *tools*
  (:require (poc.tools bcg cmyk hsl lab luv convolution gauss unsharpmask
		       fftwview histogram levels)
	    (poc.plots histograms)))

(import-swt)

(defn open-file-dialog [parent]
  (when-let [f (.open (doprops (FileDialog. parent SWT/OPEN)
			       :filter-extensions (into-array ["*.jpg;*.png;*.bmp;*.swftw"])))]
    (open-file f)))

(defn save-file-dialog [parent]
  (when-let [f (.open (doprops (FileDialog. parent SWT/SAVE)
			       :filter-extensions (into-array ["*.png;*.swftw"])))]
    (if (.endsWith (.toLowerCase f) ".swftw")
      (try
      (let [dialog (Shell. parent (reduce bit-or [SWT/APPLICATION_MODAL SWT/TITLE]))
	    layout (MigLayout. "wrap 2" "[grow,fill,300::][fill,100::]")
	    slider (Scale. dialog SWT/HORIZONTAL)
	    label (Label. dialog SWT/HORIZONTAL)
	    ok-button (Button. dialog SWT/PUSH)
	    cancel-button (Button. dialog SWT/PUSH)]
	(doprops slider :minimum 1 :maximum 100 :selection 50
		 :+selection.widget-selected
		 (.setText label (str (.getSelection slider))))
	(doprops label :text "50")
	(doprops ok-button :text "OK" :+selection.widget-selected
		 (do (save-swftw f (.getSelection slider))
		     (.close dialog)))
	(doprops cancel-button :text "Zapomnij"
		 :+selection.widget-selected (.close dialog))
	(doprops dialog :layout layout :text "Wybierz jakość"
		 :size ^unroll (450 150))
	(.open dialog))
      (catch Exception e (.printStackTrace e)))
      (save-png f))))

(defn toolbar-buttons [arg-map]
  (doall (map (fn [[label f type]]
		(if label
		  (let [button (ToolItem. (:toolbar arg-map) (if type type SWT/PUSH))]
		    (doprops button
			     :text label
			     :+selection.widget-selected
			     (try (f (assoc arg-map :button button))
				  (catch IllegalArgumentException e
				    (f)))))
		  (ToolItem. (:toolbar arg-map) SWT/SEPARATOR)))
	      [["Wczytaj" #(do (open-file-dialog (:shell %))
			      (reset-tools))]
	       ["Złomuj" #(save-file-dialog (:shell %))]
	       []
	       ["Zastosuj" #(do (send-task *data* apply-changes)
				(reset-tools))]
	       ["Cofnij" #(do (send-task *data* cancel-changes)
			      (reset-tools))]
	       []
	       ["Pokaż wykresy" #(do (doprops (:plots arg-map)
					      :layout-data
					      (str "span 2, grow, height "
						   (if (-> % :button .getSelection)
						     (str (+ 6 poc.plots/*height*) "!") "0!")))
				     (.layout (:shell arg-map)))
				   SWT/CHECK]])))

(defn expand-bar [parent]
  (let [expand-bar (ExpandBar. parent SWT/V_SCROLL)]
    (doseq [[label panel] (tool-panels expand-bar)]
      (let [expand-item (ExpandItem. expand-bar SWT/NONE)]
	(doprops expand-item
		       :text label
		       :control panel
		       :expanded false
		       :height (-> panel (.computeSize SWT/DEFAULT SWT/DEFAULT) .y))))
    expand-bar))

(defn main-window
  "Create main window."
  []
  (let [shell (Shell. (default-display))
	layout (MigLayout. "wrap 2"
			   "0[grow,fill,100::]0[fill,340::]0"
			   "0[]0[][grow,fill]0")
	plots (Composite. shell SWT/NONE)
	plots-layout (MigLayout. "" "" (format "3[%d]3" poc.plots/*height*))
	plot-canvases (plot-canvases plots)
	canvas (canvas shell)
	toolbar (ToolBar. shell (reduce bit-or [SWT/HORIZONTAL SWT/WRAP SWT/FLAT]))
	toolbar-buttons (toolbar-buttons {:shell shell :plots plots
					  :canvas canvas :toolbar toolbar})
	;;expand-bar-scroll (ScrolledComposite. shell (bit-or SWT/BORDER SWT/V_SCROLL))
	expand-bar (expand-bar shell)]
    (doprops plots
	     :layout-data "span 2, height 0!, grow, width 100::"
	     :layout plots-layout)
    (doprops canvas
	     :layout-data "span 1 2, grow"
	     :background (Color. (default-display) 100 100 100))
    (doprops shell
	     :layout layout
	     :text "POC drugiej instancji / SWftw"
	     :size ^unroll (980 700)
	     :maximized true)
    (.open shell)
    shell))