(ns poc.whole-image
  "Convertions that operate on image as a whole."
  (:import (poc ByteWorker)))

(def *operations* (atom ()))

(defn perform-operations [image-data]
  (doseq [{:keys [a f]} @*operations*]
    (f image-data @a)))

(defn add-operation-watches [f]
  (doseq [{:keys [a n]} @*operations*]
    (add-watch a (keyword n) (fn [_ _ _ _] (f)))))

(defmacro defoperation [n struct f]
  "Add operation named by n, with control structutre struct and
  function f that performs operation. f should take 2 arguments:
  image-data and control structure value"
  (let [atom-name# (symbol (str n "-control"))]
    `(do (def ~n ~f)
	 (def ~atom-name# (atom ~struct))
	 (swap! *operations* #(cons {:f ~f :a ~atom-name# :n ~(name n)} %)))))

(defoperation cmyk
  {:c 0 :m 0 :y 0 :k 0}
  (fn [image-data {:keys [c m y k]}]
    (ByteWorker/cmykCorrection image-data image-data c m y k)))


  