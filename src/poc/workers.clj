(ns poc.workers
  "Abstract workers, that always do only the last action that was
  given to them.")

(defn lie
  "A modification of a promise that accepts multiple delivers."
  []
  (let [d (java.util.concurrent.CountDownLatch. 1)
	v (atom nil)]
    (reify clojure.lang.IDeref
	   (deref [_] (.await d) @v)
	   clojure.lang.IFn
	   (invoke [this x] (locking d
			      (reset! v x)
			      (.countDown d)
			      this)))))

(defprotocol IWorker
  (send-task [impl] [impl f] [impl f args])
  (running? [impl])
  (stop [impl]))

(defn worker-activity
  [state worker]
  (println "ACt!")
  (let [running (running? worker)
	[f & args] @@(.task worker)] ;; deref atom and then lie
    (when running
      (println "run!" f args)
      (swap! (.task worker) (fn [_] (lie))) ;; setup a new promise
      (send-off (.agent worker) worker-activity worker)
      (apply f state args))))

(deftype Worker [agent task running]
  IWorker
  (send-task [impl] (send-task impl identity nil))
  (send-task [impl f] (send-task impl f nil))
  (send-task [impl f args] (deliver @task (cons f args)))
  (running? [impl] @running)
  (stop [impl]
	(reset! running false)
	(send-task impl))

  ;; This is necessary since we want to proxy all IRefs call to the
  ;; agent:
  clojure.lang.IRef
  (deref [impl] @agent)
  (setValidator [impl f] (.setValidator agent))
  (getValidator [impl] (.getValidator agent))
  (addWatch [impl k f] (.addWatch agent k f))
  (getWatches [impl] (.getWatches agent))
  (removeWatch [impl k] (.removeWatch agent k)))
	       
(defn new-worker [state]
  (let[worker (Worker. (agent state) (atom (promise)) (atom true))]
    (send-off (.agent worker) worker-activity worker)
    worker))
