(ns teod.reflect.pandoc
  "Pandoc-powered parsing and Hiccup generation from plain text"
  (:require
   [clojure.data.json :as json]
   [clojure.java.shell :refer [sh]]))

(defn -parse [{:keys [source format]}]
  (assert source)
  (assert format)
  (assert (#{"markdown" "org"} format)) ; whitelist - avoid errors
  (-> (sh "pandoc" "-f" format "-t" "json" :in source)
      :out
      (json/read-str :key-fn keyword)
      :blocks))

(defn org-> [source]
  (-parse {:source source
           :format "org"}))

(defn markdown-> [source]
  (-parse {:source source
           :format "markdown"}))

(defn header?
  "Validates a pandoc 3-arity header - level, meta, content"
  [el]
  (and (= "Header" (:t el))
       (<= 3 (count (:c el)))
       (let [level (get (:c el) 0)]
         (<= 1 level 6))))

(defn ->hiccup* [form opts]
  (cond (= (:t form)
           "Para")
        (into [:p]
              (map #(->hiccup* % opts) (:c form)))

        (= (:t form)
           "Str")
        (:c form)

        (= (:t form)
           "Plain")
        (into [:span]
              (map #(->hiccup* % opts) (:c form)))

        (= (:t form)
           "Space")
        " "

        (= (:t form)
           "Emph")
        (into [:em]
              (map #(->hiccup* % opts) (:c form)))

        (= (:t form)
           "BulletList")
        (into [:ul]
              (for [raw-list-item (:c form)]
                (into [:li]
                      (map #(->hiccup* % opts)
                           raw-list-item))))

        ;; otherwise -- don't return!
        :else
        nil))

(defn ->hiccup
  ([data] (->hiccup data {}))
  ([data opts]
   (into [:div]
         (for [p data]
           (->hiccup* p opts)))))
