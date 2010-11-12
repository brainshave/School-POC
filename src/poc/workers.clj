(ns poc.workers
  "Abstract workers, that always do only the last action that was
  given to them.")

(defprotocol IWorker
  (send-task [impl] [impl f] [impl f args])
  (running? [impl])
  (stop [impl]))

(defn worker-activity
  [state worker]
  (let [running (running? worker)
	[f & args] (-> worker .task deref deref)] ;; deref atom and then promise
    (when running
      (reset! (.task worker) (promise)) ;; setup a new promise
      (send (.agent worker) worker-activity worker)
      (apply f state args))))

(deftype Worker [agent task running]
  IWorker
  (send-task [impl] (send-task impl identity nil))
  (send-task [impl f] (send-task impl f nil))
  (send-task [impl f args]
	     (let [new-task (cons f args)]
	       (try (deliver @task new-task)
		    (catch IllegalStateException e ;; value was alerady delivered
		      (reset! task (deliver (promise) new-task))))))
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
    (send (.agent worker) worker-activity worker)
    worker))
