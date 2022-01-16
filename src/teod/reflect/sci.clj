(ns teod.reflect.sci
  (:refer-clojure :exclude [eval])
  (:require [sci.core :as sci]))

;; consider removing - indirection might not be worth it
;;
;; works, though.
(defn link [x]
  [:a {:href x} x])

(defn ctx []
  (sci/init {:namespaces {'teod {'link link}}}))

;; Problem: I need to pass some kind of context to eval. That means ... I need a
;; convention for the first arg called.
;;
;; Or ... attach some metadata given a keyword that I could pull out.
;;
;; :thinking_face:
;;
;; But ... that metadata map can then /not/ be attached to certain objects. So
;; perhaps just having a config map as the first param is the best option, and
;; just ignore it (_opts) when it's not needed.
(defn eval [form]
  (sci/eval-form (ctx)
                 form))
