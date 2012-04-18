(ns async-kit.core
  (:use-macros [async-kit.macros :only [async let-async]]))

(defprotocol Future
  (error [this]
    "gives you the error associated with this request or nil if no error")
  (completed? [this]
    "true if request is completed")
  (await* [this callback]
    "subscribe to the future if it hasent alreddy completed then
   executes callback directly"))

(defrecord AsyncFuture [
                        value       ;; atom for value nil if not completed 
                        error-a      ;; atom for error nil if no error
                        subscribers ;; subscribers atom to the future [] default
                        ]
  
  Future
  (error [this] @error-a)
  
  (completed? [this] (or @error-a @value))
  
  (await* [this callback]
    (cond 
     (error this) (callback (error this) nil)
     @value-state (callback nil @value)
     :else        (swap! subscribers conj callback))))
 

(defn async* [callback]
  (let [value-delivered (atom false)
        future (new AsyncFuture (atom nil) (atom nil) (atom []))
        value-cb
        (fn [v]
          (cond 
           (error future) (throw "exception: AsyncFuture recived a value after alreddy thrown a error")
           @value-delivered (throw "exception: tried to deliver value multiple times to a AsyncFuture")
           :else (do 
                   (reset! value-delivered true)
                   (reset! (:value future) v)
                   (doseq [sub @(:subscribers future)]
                     (sub nil v)))))
        
        error-cb
        (fn [e]
          (cond
           @value-delivered (throw "error thrown on alreddy delivered future")
           :else (do (reset! (:error-a future) e)
                     (doseq [sub @(:subscribers future)]
                       (sub e nil)))))]
    (callback value-cb error-cb)
    future))



