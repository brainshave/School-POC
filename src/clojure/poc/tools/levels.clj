(ns poc.tools.levels
  (:use (poc tools simpletool utils))
  (:import poc.ByteWorker))

(def in-start-key "Początek wejścia:")
(def in-end-key "Koniec wejścia:")
(def out-start-key "Początek wyjścia:")
(def out-end-key "Koniec wyjścia:")

(defn levels [{in-start in-start-key
	       in-end in-end-key
	       out-start out-start-key
	       out-end out-end-key}
	      data-in data-out]
  (let [factor (-> (/ (- out-end out-start) (- in-end in-start))
		   (try (catch ArithmeticException e 255)))
	m (->>
	   (map #(-> % (- in-start) (* factor) (+ out-start))
		(range 256))
	   (map int)
	   to-byte-array)]
    (ByteWorker/applyMaps data-in data-out m m m)))

(add-tool 22 (simple-tool "Poziomki"
			  levels
			  [in-start-key 0 0 255]
			  [in-end-key 255 0 255]
			  [out-start-key 0 0 255]
			  [out-end-key 255 0 255]))
