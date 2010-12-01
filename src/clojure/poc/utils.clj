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