(ns poc.fftw
  (:import com.schwebke.jfftw3.JFFTW3))

(defn malloc [size] (JFFTW3/jfftw_complex_malloc size))

(defmacro defplanner [direction]
  (let [dir (symbol (reduce str "JFFTW3/JFFTW_"
			      (map #(Character/toUpperCase %) (name direction))))
	name (symbol (str (name direction) "-plan"))]
  `(defn ~name
     (~'[width height inout] ~(cons name '(width height inout inout)))
     (~'[width height in out] ~(cons name '(width height in out 0)))
     (~'[width height in out flags]
      ~(concat '(JFFTW3/jfftw_plan_dft_2d width height in out) [dir] '(flags))))))

(defplanner forward) ;; defn forward-plan
(defplanner backward) ;; defn backward-plan

(defmacro with-fftw
  [[symbol spec & rest-specs] & body]
  `(let [~symbol ~spec]
     (try
       ~(if rest-specs
	  `(with-fftw ~rest-specs ~@body)
	  `(do ~@body))
       (finally (JFFTW3/jfftw_complex_free ~symbol)))))
    
  

;; obraz wejsciowy -> 2x wieksza tablica double'i (*16) i (co drugi element zaczynajac od 0-elementu)
;; tworzenie "planu", okreslenie obrazu, w ktora strone, itd.
;; rysowanie : przepuścić przez logarytm przed rysowaniem, skalowanie od minimalnej do maksymalnej wartości
;; pamiętać o free

