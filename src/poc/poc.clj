(ns poc.poc
  "Przetwarzanie obrazów cyfrowych"
  (:gen-class)
  (:require (little-gui-helper [properties :as props])
	    (poc [ui-elements :as ui]
		 [image :as image]
		 [transformations :as transformations]))
  (:import (org.eclipse.swt.widgets Display Shell Menu MenuItem
				    FileDialog Canvas Label)
	   (org.eclipse.swt.layout FillLayout)
	   (org.eclipse.swt.events SelectionListener PaintListener
				   MouseMoveListener MouseListener
				   ControlListener)
	   (org.eclipse.swt.graphics GC Image ImageData)
	   (org.eclipse.swt SWT)
	   (net.miginfocom.swt MigLayout)))


(defn make-gui []
  (let [shell (Shell.)
	layout (MigLayout. "" "0[grow,fill,100::]0[grow,fill,30:30:300]0" "0[grow,fill]0")
	scroll-previous-point (atom nil)
	canvas (Canvas. shell SWT/NONE)
	menu-bar (ui/make-menu-bar shell canvas)
	expand-bar (ui/make-expand-bar shell)]
    ;;(props/doprops label :text "ASDF")
    (add-watch image/*original-data* :realign-image (fn [_ _ _ loaded-image-data]
						      (image/realign-image canvas loaded-image-data)))
    (add-watch image/*image* :canvas-refresh (fn [_ _ _ new-val]
     					       (.asyncExec (Display/getDefault)
							   #(when new-val
							      (.redraw canvas)))))
    (props/doprops canvas
		   ;;:layout-data "grow"
		   :+paint.paint-control
		   (do
		     ;;(println "Przerysowywuje obszar")
		     (when-let [img @image/*image*]
		       (let [[x y] @image/*scroll-delta*]
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
			   
			   (swap! image/*scroll-delta*
				  (fn [[act-x act-y]]
				    [(+ act-x scroll-x)
				     (+ act-y scroll-y)]))))
		       (swap! scroll-previous-point
			      (fn [_] [(.x event) (.y event)])))
		     ;; set previus point to nil when mouse button is up
		     (swap! scroll-previous-point (fn [_] nil))))
    (props/doprops shell
		   :text "Przetwarzanie Obrazów Cyfrowych / Szymon Witamborski"
		   :menu-bar menu-bar
		   :layout layout
		   :size ^unroll (980 700))
    (.open shell)
    shell))

(defn -main [& args]
  (let [display (Display/getDefault)
	shell (make-gui)]
    (transformations/add-all-transformations)
    (if-let [file-name (first args)]
      (image/open-file file-name))
    (.open shell) 
    (loop [disposed false]
      (if (not (.readAndDispatch display))
	(.sleep display))
      (if disposed
	(do (swap! image/*image*
		   (fn [image]
		     (if image (.dispose image))
		     nil))
	    (.dispose display))
	(recur (.isDisposed shell))))))