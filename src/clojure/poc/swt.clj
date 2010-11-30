(ns poc.swt
  "Functions for Happy SWT User")
  
(defn import-swt
  "Convenience function to import all needed SWT classes in project"
  []
  (import [org.eclipse.swt SWT]
	  [org.eclipse.swt.widgets
	   Shell Menu MenuItem MessageBox
	   FileDialog ExpandBar ExpandItem
	   Composite Label Scale Canvas Display
	   Button ToolBar ToolItem]
	  [org.eclipse.swt.custom ScrolledComposite]
	  [org.eclipse.swt.graphics Color GC ImageData]
	  [org.eclipse.swt.events SelectionListener PaintListener]
	  [net.miginfocom.swt MigLayout]))

;; import swt here
(import-swt)

(defn default-display
  "Get default display"
  []
  (Display/getDefault))

(defn swt-loop
  "Loop that dispatches SWT events. Catches all exceptions and simply
  prints stack traces."
  ([shell]
     (let [display (default-display)]
       (try 
	 (if-not (.readAndDispatch display)
	   (.sleep display))
	 (catch Exception e (.printStackTrace e)))
       (if (or (and shell (not (.isDisposed shell)))
	       (not shell))
	 (recur shell)
	 (.dispose display))))
  ([] (swt-loop nil)))

(defn async-exec
  "Put asynchronously function to evaluate in swt in near future."
  [f & args]
  (.asyncExec (default-display) #(apply f args)))


(defn message [title body]
  (async-exec #(-> (default-display) .getShells first
		   (MessageBox. (reduce bit-or [SWT/ICON_ERROR, SWT/OK]))
		   (doto (.setText title) (.setMessage body) .open))))
  
  
