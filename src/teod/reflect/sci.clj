(ns teod.reflect.sci
  (:refer-clojure :exclude [eval])
  (:require [sci.core :as sci]))

;; clojure.java.shell/sh

(defn link [x]
  [:a {:href x} x])

(defn ^:private map-vals [f m]
  (into {}
        (map (fn [[k v]]
               [k (f v)])
             m)))

(let [{:keys [sci-env]} (quote {:sci-env {teod {slurp clojure.core/slurp
                                                sh clojure.java.shell/sh}}})]
  (map-vals (fn [m]
              (map-vals requiring-resolve m))
            sci-env))

(defn ctx [env]
  (sci/init {:namespaces env}))

(defn eval [{:keys [sci-env]} form]
  (let [env (map-vals (fn [m]
                        (map-vals requiring-resolve m))
                      sci-env)]
    (sci/eval-form (ctx env)
                   form)))
