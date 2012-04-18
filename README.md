# Async-kit for clojurescript

``` 
;; Async-kit examples

(ns example
 (:require [async-kit.core :as async-kit])
 (:use-macros [async-kit.macros :only [let-async async]]))

;; first wrap setTimeout

(defn wait [time]
 (async [complete error]
  (js/setTimeout
   (fn [] (complete true)))))

;; and wrap jQuery .ajax

(defn ajax [& {:keys [method url data-type]}]
  (async
   [complete error]
   (.ajax
    js/jQuery
    (clj->js
     {:url url
      :method method
      :data-type data-type
      :error error
      :sucess complete}))))

;; now whe can write some async code

(defn some-async-render-stuf-thing []
  (let-async
   [json-data (ajax :method "GET" :url "/something" :data-type "json")
    :let pd   (prosess-data json-data) ;; normal let
    _         (wait 2000) ;; wait for 2 secs cuz why not
    ]
   (render-some-page pd))) ;; renders-the-page
   ;; the let-async macro returns a async it self to


;; make a function doing something each time a event fires
;; obs! (:use [async-kit.event :only [await-event]])

(defn log-errors []
  (let-async
   [error (await-event :error)]
   ;; note await-event only fires once
   ;; (witch is a requirement of AsyncFuture)
   (ajax :method "POST" :url "/log/error" :data error)
   (log-errors)))
