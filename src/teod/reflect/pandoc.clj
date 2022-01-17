(ns teod.reflect.pandoc
  "Pandoc-powered parsing and Hiccup generation from plain text"
  (:require
   [clojure.data.json :as json]
   [clojure.java.shell :refer [sh]]
   [clojure.walk :refer [prewalk postwalk]]
   [clojure.string :as str]))

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

(comment
  (org-> "some text"))

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
           "Space")
        " "

        (= (:t form)
           "Emph")
        (into [:em]
              (map #(->hiccup* % opts) (:c form)))

        ;; otherwise -- don't return!
        :else
        nil))

(defn ->hiccup2
  ([data] (->hiccup2 data {}))
  ([data opts]
   (into [:div]
         (for [p data]
           (->hiccup* p opts)))))

(defn ->hiccup
  ([data]
   (->hiccup data {}))
  ([data {:keys [debug]}]
   (into [:div]
         (prewalk (fn [el]
                     (cond
                       (= "Str" (:t el))
                       (:c el)

                       (= "Space" (:t el))
                       " "

                       (= "Para" (:t el))
                       (into [:p
                              (if (every? string? (:c el))
                                (str/join "" (:c el))
                                (:c el))])

                       (= "Plain" (:t el))
                       (into [:span
                              (if (every? string? (:c el))
                                (str/join "" (:c el))
                                (:c el))])

                       (= "BulletList" (:t el))
                       (into [:ul]
                             (for [li (:c el)]
                               (into [:li] li)))

                       (header? el)
                       (let [[level _attrs content] (:c el)]
                         (into [(keyword (str "h" level))]
                               content))

                       :else
                       el))
                   data))))

(defn org->hiccup
  ([data]
   (org->hiccup data {}))
  ([data opts]
   (-> data
       org->
       (->hiccup opts))))

(defn markdown->hiccup
  ([data]
   (-> data markdown-> ->hiccup))
  ([data opts]
   (-> data markdown-> ->hiccup)))

