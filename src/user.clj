(ns user)

;; (clojure "-X teod.reflect.build/watch!")

(defn watch
  ([]
   (watch {}))
  ([opts]
   (let [w (requiring-resolve 'teod.reflect.build/watch!)]
     (w opts))))
