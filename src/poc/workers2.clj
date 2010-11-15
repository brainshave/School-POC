(ns poc.workers2)

(defn hat
  "Returns an object similar promise, but:
   1. You can deliver value many times. old value will be forgotten.
   2. You can deref value only once. After deref, box is emptyied.

   So, looks more like an opposite of \"promise\"."
  []
  (let [s (java.util.concurrent.Semaphore. 0 true)
	a (atom nil)]
    (reify clojure.lang.IDeref
	   (deref [_]
		  (.acquire s)
		  (let [v @a]
		    (reset! a nil)
		    v))
	   clojure.lang.IFn
	   (invoke [this v]
		   (.drainPermits s)
		   (reset! a v)
		   (.release s)
		   this)
	   java.lang.Object
	   (toString [_] (str "hat, " s ", " (or @a "nil"))))))

(defprotocol IWorker
  (-send-task [impl task])
  (running? [impl])
  (stop [impl]))

(defn worker [x]
  (let [state (atom x)
	h (hat)
	running (atom true)
	thread (Thread. #(when @running
			   (try
			     (let [[f & args] @h]
			       (apply swap! state f args))
			     (catch Exception e
			       (.printStackTrace e)))
			   (recur)))]
    (.start thread)
    (reify
     IWorker
     (-send-task [_ task] (h task))
     (running? [_] @running)
     (stop [_]
	   (reset! running false)
	   (.interrupt thread))
     
     clojure.lang.IRef
     (deref [_] @state)
     (setValidator [_ f] (.setValidator state f))
     (getValidator [_] (.getValidator state))
     (addWatch [_ k f] (.addWatch state k f))
     (getWatches [_] (.getWatches state))
     (removeWatch [_ k] (.removeWatch state k)))))

(defn send-task
  ([worker]
     (-send-task worker '(identity))
     worker)
  ([worker f & args]
     (-send-task worker (cons f args))
     worker))