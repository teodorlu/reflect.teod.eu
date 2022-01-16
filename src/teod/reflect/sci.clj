(ns teod.reflect.sci
  (:refer-clojure :exclude [eval])
  (:require [sci.core :as sci]))

;; clojure.java.shell/sh

(defn link [x]
  [:a {:href x} x])

(defn ctx []
  (sci/init {:namespaces {'teod {'link link}}}))

(defn eval [{:keys [sci-env]} form]
  (sci/eval-form (ctx)
                 form))
