(ns poc.poc
  "Przetwarzanie obrazów cyfrowych"
  (:require (little-gui-helper [properties :as props]))
  (:import (org.eclipse.swt.widgets Display Shell Menu MenuItem
				    FileDialog Canvas)
	   (org.eclipse.swt.layout FillLayout)
	   (org.eclipse.swt.events SelectionListener PaintListener
				   MouseMoveListener MouseListener)
	   (org.eclipse.swt.graphics GC Image)
	   (org.eclipse.swt SWT)))


;;(defonce *display* (Display.))

(defn open-file [file-name]
  (let [new-image (Image. (Display/getDefault)
			  file-name)]
    (swap! *image* (fn [current-image]
		     (if current-image
		       (.dispose current-image))
		     new-image))))

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
				   (open-file file-name)
				   (.update canvas)))
	exit-item (props/doprops (MenuItem. file-menu SWT/PUSH)
				 :text "&Wyjdź\tCtrl+Q"
				 :accelerator (+ SWT/MOD1 (int \Q))
				 :+selection.widget-selected (.close shell))]
    (props/doprops file-item
		   :menu file-menu)
    menu-bar))

(def *image* (atom nil))

(defn make-gui []
  (let [shell (Shell.)
	layout (FillLayout.)
	scroll-delta (atom nil)
	start-point (atom [0 0])
	canvas (Canvas. shell (reduce bit-or [SWT/NO_REDRAW_RESIZE]))
	menu-bar (make-menu-bar shell canvas)]
    (props/doprops canvas
		   :+paint.paint-control
		   (do
		     ;; (println "Przerysowywuje obszar")
		     (when-let [img @*image*]
		       (let [[x y] @start-point]
			 (.. event gc (drawImage img x y)))))
		   :+mouse-move.mouse-move
		   (if (and @*image*
			    (-> event .stateMask
				(bit-and SWT/BUTTON1) (not= 0)))
		     (let [[x y] @scroll-delta]
		       (if-not x 
			 (swap! scroll-delta (fn [_] [(.x event)
						      (.y event)]))
			 (let [dx (- (.x event) x)
			       dy (- (.y event) y)
			       rect (.getBounds @*image*)]
			   ;(println dx dy)
			   (.scroll canvas dx dy 0 0
				    (.width rect) (.height rect) false))))
		     (do (swap! start-point
				(fn [[start-x start-y]]
				  (let [[scroll-x scroll-y] @scroll-delta]
				    [(- start-x (- (.x event) scroll-x))
				     (- start-y (- (.y event) scroll-y))])))
			 (swap! scroll-delta (fn [_] nil)))))
    (props/doprops shell
		   :text "ASDF"
		   :menu-bar menu-bar
		   :layout layout
		   :size ^unroll (300 200))
    (.open shell)
    shell))

(defn main- [& args]
  (let [display (Display/getDefault)
	shell (make-gui)]
    (if-let [file-name (first args)]
      (open-file file-name))
    (.open shell) 
    (loop [disposed false]
      (if (not (.readAndDispatch display))
	(.sleep display))
      (if disposed
	(do (swap! *image* (fn [image]
			 (if image (.dispose image))
			 nil))
	    (.dispose display))
	(recur (.isDisposed shell))))))