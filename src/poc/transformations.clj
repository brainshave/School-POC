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
	      ;; gamma: 
	      (/ 256) double (Math/pow (/ 1 gamma)) (* 256)
	      ;; contrast:
	      (- 128) (* (Math/tan (* 1/2 Math/PI
				      (/ (+ contrast 128) 256))))
	      (+ 128)
	      ;; brightness
	      (+ brightness)
	      (try (catch ArithmeticException e 255)))
	 color-map)))

(def *balance-histograms*
     (atom {:r? false :g? false :b? false :rgb? false}))

(defn apply-histogram-balancing
  "This transformation works only on discrete 0-255 values of reds,
  greens, and blues"
  [{:keys [r? g? b? rgb?]}  reds greens blues]
  (let [histograms @image/*original-histograms*
	size (-> histograms meta :size)
	factor (/ 256 size)
	[red-hist green-hist blue-hist] histograms]
    (for [[apply? color-map histogram] [[r? reds red-hist]
					[g? greens green-hist]
					[b? blues blue-hist]]]
      (if-not apply? color-map
	      ;; cumulus - cumulating histogram
	      (let [cumulus (reduce #(conj %1 (+ (peek %1) %2))
				    [(first histogram)] (rest histogram))]
		(map #(* factor (cumulus %1)) color-map))))))

(defn add-all-transformations []
  (image/add-transformation 5 apply-histogram-balancing
			    *balance-histograms*)
  (image/add-transformation 10 apply-brightness-contrast-gamma
			    *brightness-contrast-gamma*))