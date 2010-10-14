(ns poc.poc
  "Przetwarzanie obrazów cyfrowych"
  (:require (little-gui-helper [properties :as props]))
  (:import (org.eclipse.swt.widgets Display Shell Menu MenuItem
				    FileDialog)
	   (org.eclipse.swt.events SelectionListener)
	   (org.eclipse.swt SWT)))


;;(defonce *display* (Display.))

(defn gui []
  (let [shell (Shell.)
	menu-bar (Menu. shell SWT/BAR)
	file-item (props/doprops (MenuItem. menu-bar SWT/CASCADE)
				 text "&Plik")
	file-menu (Menu. shell SWT/DROP_DOWN)
	file-dialog (props/doprops (FileDialog. shell SWT/OPEN)
				   {})
	open-item (props/doprops (MenuItem. file-menu SWT/PUSH)
				 text "&Otwórz\tCtrl+O"
				 accelerator (+ SWT/MOD1 (int \O))
				 +selection.widget-selected
				 (print "asdf"))]
    (props/doprops file-item
		   menu file-menu)
    (props/doprops shell
		   text "ASDF"
		   menu-bar menu-bar
		   size ^unroll (300 200))
    (.open shell)
    shell))

(defn main- [& args]
  (let [display (Display/getDefault)
	shell (gui)]
    (.open shell) 
    (loop [disposed false]
      (if (not (.readAndDispatch display))
	(.sleep display))
      (if disposed
	(.dispose display)
	(recur (.isDisposed shell))))))