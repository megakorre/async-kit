(ns async-kit.macros)

(defmacro async [bindings & body]
 `(async-kit/async* 
   (fn ~bindings ~@body))) 

(defn make-bindings 
  [remaining-bindings body completed-sym error-sym]
 
  (cond
   (= (count remaining-bindings) 1) 
   (throw "wrong number of bindings in let-async needs to be event")
   
   (= (count remaining-bindings) 0)
   `(~completed-sym (do ~@body))

   (= (first remaining-bindings) :let)
   (let [[_ n expr & rest] remaining-bindings]
     `(let [~n ~expr]
        ~(make-bindings rest body completed-sym error-sym)))
   
   :else
   (let [[binding-name binding-expression & rest]
         remaining-bindings]
     
     (cond
      (= (first (name binding-name)) "-")
      `(let [~binding-name ~binding-expression]
         ~(make-bindings rest body completed-sym error-sym))
     
      (= binding-name :if-error)
      (let [new-error-sym (gensym "error")]
        `(let [~new-error-sym ~(cons 'fn binding-expression)]
           ~(make-bindings rest body completed-sym new-error-sym)))
   
   
      :else
      `(async-kit/await* 
        ~binding-expression
        (fn [error# value#]
          (if error#
            (~error-sym error#)
            (let [~binding-name value#] 
              ~(make-bindings rest body completed-sym error-sym)))))))))


(defmacro let-async [bindings & body]
  (let [complete-sym (gensym "completed")
        error-sym    (gensym "error")]
    `(async-kit/async*
      (fn [~complete-sym ~error-sym]
        ~(make-bindings bindings body complete-sym error-sym)))))


(comment 
 ;; some sudo code 

 (let-async [some-site (http :GET "/something")
             :let some-normal-exp (process some-site)
             some-data (-> parse-site :user-id (xhr "/something"))]

  (render-something "some-template" some-data)))

