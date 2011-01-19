(ns poc.tools.fftwview
  (:use (poc tools simpletool utils fftw swt)
	(little-gui-helper properties))
  (:import poc.DoubleWorker
	   com.schwebke.jfftw3.JFFTW3))

(import-swt)

(defn fftwview [render-type data-in data-out]
  (let [width (.width data-in)
	height (.height data-in)]
    (with-fftw [buff (malloc (* width height))
		plan (forward-plan height width buff)]
      (DoubleWorker/fillComplex data-in (JFFTW3/jfftw_complex_get buff))
      (JFFTW3/jfftw_execute plan)
      (DoubleWorker/render (JFFTW3/jfftw_complex_get buff) data-out render-type))))

(defrecord FFTWViewTool
  [name vs panel]
  ITool
  (reset [tool])
  (parameters [tool] vs)
  (function [tool] fftwview)
  (create-panel [tool parent]
		(let [new-panel (Composite. parent SWT/NONE)
		      layout (MigLayout.)]
		  (doseq [render-type (poc.DoubleWorker$RenderType/values)]
		    (doprops (Button. new-panel SWT/RADIO)
			     :text (str render-type)
			     :+selection.widget-selected
			     (reset! vs render-type)))
		  (reset! panel panel)
		  (doprops new-panel :layout layout))))

(add-tool 91 (FFTWViewTool. "Podgląd FFTW" (atom nil) (atom nil)))