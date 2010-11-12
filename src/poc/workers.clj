(ns poc.workers
  "Abstract workers, that always do only the last action that was
  given to them.")

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
  (stop [impl] 
  
  clojure.lang.IRef
  (deref [impl] @agent)
  (setValidator [impl f] (.setValidator agent))
  (getValidator [impl] (.getValidator agent))
  (getWatches [impl] (.getWatches agent))
  (addWatch [impl k f] (.addWatch agent k f))
  (removeWatch [impl k] (.removeWatch agent k)))
	       
(defn new-worker [state]
  (Worker. (agent state) (atom (promise)) (atom true)))

	       
(defn- worker-thread
  "Function to work inside workers agent."
  [state worker]
  (let [{:keys [agent task running]} @worker
	f (first @task)
	args (rest @task)]
    (when running
      (swap! worker #(assoc % :task (promise))) ;; setup a new promise
      ;; for next task
      (send agent worker-thread worker) ;; send self == looping
      (apply f state args)))) ;; do actual work

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