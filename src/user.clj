(ns user
  (:require [clojure.repl :refer :all]))

;; (clojure "-X teod.reflect.build/watch!")

(defn watch
  ([]
   (watch {}))
  ([opts]
   (let [w (requiring-resolve 'teod.reflect.build/watch!)]
     (w opts))))
