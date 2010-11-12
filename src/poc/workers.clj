(ns poc.workers
  "Abstract workers, that always do only the last action that was
  given to them.")
	       
(defn- worker-activity--
  "Function to work inside workers agent."
  [state worker]
  (let [
	f (first @task)
	args (rest @task)]
    (when running
      (swap! worker #(assoc % :task (promise))) ;; setup a new promise
						;; for next task
      (send agent worker-activity worker) ;; send self == looping
      (apply f state args)))) ;; do actual work

(defn worker-activity
  [state worker]
  (let [running (running? worker)
	[f & args]  @(.task worker)]
    (when running
      (reset! (.task worker) (promise)) ;; setup a new promise
      (send (.agent worker) worker-activity worker)
      (apply (f state args)))))

(defprotocol IWorker
  (send-task [impl f & args])
  (running? [impl])
  (stop [impl]))

(deftype Worker [agent task running]
  IWorker
  (send-task [impl f & args]
	     (let [new-task (cons f args)]
	       (try (deliver @task new-task)
		    (catch IllegalStateException e ;; value was alerady delivered
		      (reset! task (deliver (promise) new-task))))))
  (running? [impl] @running)
  (stop [impl] (reset! running false))
  
  clojure.lang.IRef
  (deref [impl] @agent)
  (setValidator [impl f] (.setValidator agent))
  (getValidator [impl] (.getValidator agent))
  (addWatch [impl k f] (.addWatch agent k f))
  (getWatches [impl] (.getWatches agent))
  (removeWatch [impl k] (.removeWatch agent k)))
	       
(defn new-worker [state]
  (Worker. (agent state) (atom (promise)) (atom true)))

(defn worker--
  "Return new worker."
  [state]
  (let [new-worker (atom {:agent (agent state)
			  :task (promise)
			  :running true})]
    (send (:agent @new-worker) worker-thread new-worker)
    new-worker))

(defn send-task--
  "Send a task to worker. f has 1 + args arguments. First argument is
  current worker state."
  [worker f & args]
  (let [task (cons f args)]
    (try (deliver (:task @worker) task)
	 (catch IllegalStateException e ;; value was already delivered
	   (swap! worker #(assoc % :task (deliver (promise) task)))))))

(defn stop-worker--
  [worker]
  (swap! worker #(assoc % :running false)))
  ;;(send-task worker nil))