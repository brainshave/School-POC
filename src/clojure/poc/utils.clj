(ns poc.utils
  "Unrelated functions.")

(defn ok?
  "Check if w is not null and not disposed."
  [w]
  (and w (not (.isDisposed w))))