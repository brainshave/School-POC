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


(def *image* (atom nil))
(def *scroll-delta* (atom [0 0]))

(defn open-file [file-name]
  (let [new-image (Image. (Display/getDefault)
			  file-name)]
    (swap! *image* (fn [current-image]
		     (if current-image
		       (.dispose current-image))
		     new-image))
    (swap! *scroll-delta* (fn [_] [0 0]))))

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
				   (.redraw canvas)))
	exit-item (props/doprops (MenuItem. file-menu SWT/PUSH)
				 :text "&Wyjdź\tCtrl+Q"
				 :accelerator (+ SWT/MOD1 (int \Q))
				 :+selection.widget-selected (.close shell))]
    (props/doprops file-item
		   :menu file-menu)
    menu-bar))


(defn make-gui []
  (let [shell (Shell.)
	layout (FillLayout.)
	scroll-previous-point (atom nil)
	canvas (Canvas. shell SWT/NONE)
	menu-bar (make-menu-bar shell canvas)]
    (props/doprops canvas
		   :+paint.paint-control
		   (do
		     ;;(println "Przerysowywuje obszar")
		     (when-let [img @*image*]
		       (let [[x y] @*scroll-delta*]
			 (.. event gc (drawImage img x y)))))
		   :+mouse-move.mouse-move
		   (if (-> event .stateMask
			   (bit-and SWT/BUTTON1) (not= 0))
		     (do 
		       (when-let [[prev-x prev-y] @scroll-previous-point]
			 (let [scroll-x (- (.x event) prev-x)
			       scroll-y (- (.y event) prev-y)
			       rect (.getClientArea canvas)]
			   ;;(println scroll-x scroll-y)
			   (.scroll canvas scroll-x scroll-y 0 0
				    (.width rect) (.height rect) false)
			   
			   (swap! *scroll-delta*
				  (fn [[act-x act-y]]
				    [(+ act-x scroll-x)
				     (+ act-y scroll-y)]))))
		       (swap! scroll-previous-point
			      (fn [_] [(.x event) (.y event)])))
		       ;;(.redraw canvas))
		     ;; set previus point to nil when mouse button is up
		     (swap! scroll-previous-point (fn [_] nil))))
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