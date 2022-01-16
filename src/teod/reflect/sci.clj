(ns teod.reflect.sci
  (:refer-clojure :exclude [eval])
  (:require [sci.core :as sci]))

;; clojure.java.shell/sh

(defn link [x]
  [:a {:href x} x])

(defn ctx [env]
  (sci/init {:namespaces {'user env}}))

(defn eval [{:keys [sci-env]} form]
  (let [env (reduce-kv (fn [coll k v]
                             (assoc coll k (requiring-resolve v)))
                           {}
                           sci-env)]
    (sci/eval-form (ctx env)
                   form)))
