(ns poc.swt
  "Common imports for SWT")
  
(defn import-swt []
  (import [org.eclipse.swt.widgets
	   Menu MenuItem MessageBox
	   FileDialog ExpandBar ExpandItem
	   Composite Label Scale Canvas Display
	   Button]
	   [org.eclipse.swt.custom ScrolledComposite]
	   [org.eclipse.swt SWT]
	   [org.eclipse.swt.events SelectionListener PaintListener]
	   [net.miginfocom.swt MigLayout]))

(import-swt)

(defn swt-loop []
  (try (let [display (Display/getDefault)]
	 (if-not (.readAndDispatch display)
	   (.sleep display)))
       (catch Exception e (.printStackTrace e)))
  (recur))

(defn start-async [f & args]
  (let [display (Display/getDefault)]
    (.asyncExec display
		#(let [shell (apply f args)]
		   ;;(transformations/add-all-transformations)
		   (if-let [file-name (first args)]
		     ;;(image/open-file file-name))
		   (.open shell))))))

(defn message [title body]
  (.asyncExec (Display/getDefault)
	      #(-> (Display/getDefault) .getShells first
		   (MessageBox. (reduce bit-or [SWT/ICON_ERROR, SWT/OK]))
		   (doto (.setText title) (.setMessage body) .open))))
  
  
