(ns poc.utils
  "Unrelated functions.")

(defn ok?
  "Check if w is not null and not disposed."
  [w]
  (and w (not (.isDisposed w))))

(defn bounds [w]
  (let [b (.getBounds w)] [(.width b) (.height b)]))

(defn remove-all-watchers [a]
  (doseq [k (keys (.getWatches a))]
    (remove-watch a k)))

(defn to-byte-array
  "Convert sequence to byte array"
  [seq]
  (->> seq
       (map #(.intValue %))
       (map #(cond
	      (> % 255) -1
	      (> % 127) (- % 256)
	      (< % 0) 0
	      true %))
       (map byte)
       (into-array Byte/TYPE)))

(defn array-copy [from to]
  (System/arraycopy from 0 to 0 (min (count from) (count to))))