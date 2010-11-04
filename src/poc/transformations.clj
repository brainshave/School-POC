(ns poc.transformations
  (:require (poc [image :as image])))

(def *brightness-contrast-gamma*
     (atom {:brightness 0
	    :contrast 0
	    :gamma 1.0}))

(defn apply-brightness-contrast-gamma
  [{:keys [brightness contrast gamma]}
   reds greens blues]
  (for [color-map [reds greens blues]]
    (map #(-> %
	      (/ 256) double (Math/pow (/ 1 gamma)) (* 256) ;; gamma
	      (- 128) (* (Math/tan (* 1/2 Math/PI
				      (/ (+ contrast 128) 256))))
	      (+ 128)
	      ;;(- 128) (* (/ (+ contrast 128) 128)) (+ 128)
	      ;;(* (+ 1 (/ contrast 128))) (- contrast) ;; contrast
	      (+ brightness)
	      (try (catch ArithmeticException e 255)))
	 color-map)))

(def *balance-histograms*
     (atom {:r? false :g? false :b? false}))

(defn add-all-transformations []
  (image/add-transformation 10 apply-brightness-contrast-gamma
			    *brightness-contrast-gamma*))