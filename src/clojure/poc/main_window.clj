(ns poc.main-window
  "Main window for this application."
  (:use (poc swt [canvas :only [canvas]])
	(little-gui-helper properties)
	(poc)))

(import-swt)

(defn toolbar-buttons [toolbar]
  (doall (map (fn [[label]]
		(let [button (ToolItem. toolbar SWT/PUSH)]
		  (doprops button :text label)))
	      [["Otwórz"]
	       ["Zapisz"]
	       ["Zastosuj"]
	       ["Cofnij"]])))

(defn main-window
  "Create main window."
  []
  (let [shell (Shell. (default-display))
	layout (MigLayout. "wrap 2" "0[grow,fill,100::]0[fill,340!]0" "0[][grow,fill]0")
	canvas (canvas shell)
	toolbar (ToolBar. shell (reduce bit-or [SWT/HORIZONTAL SWT/WRAP SWT/FLAT]))
	toolbar-buttons (toolbar-buttons toolbar)
	;;expand-bar-scroll (ScrolledComposite. shell (bit-or SWT/BORDER SWT/V_SCROLL))
	expand-bar (ExpandBar. shell SWT/V_SCROLL)]
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