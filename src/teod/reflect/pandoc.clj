(ns teod.reflect.pandoc
  "Pandoc-powered parsing and Hiccup generation from plain text"
  (:require
   [clojure.data.json :as json]
   [clojure.java.shell :refer [sh]]
   [clojure.walk :refer [postwalk]]
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

(comment
  ;; a valid header:
  (header? {:t "Header", :c [1 ["header" [] []] ["header"]]}))

(defn ->hiccup [data]
  (into [:div]
        (postwalk (fn [el]
                    (cond
                      (= "Str" (:t el))
                      (:c el)

                      (= "Space" (:t el))
                      " "

                      (= "Para" (:t el))
                      (into [:p
                             (if (every? str (:c el))
                               (str/join "" (:c el))
                               (:c el))])

                      (= "Plain" (:t el))
                      (into [:span
                             (if (every? str (:c el))
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

                      :else el))

                  data)))


(comment
  (-> "* header\nBody goes here"
      org->
      ->hiccup)

  (org-> "* head /more/")
  (-> "- item 1\n- item 2"
      org->
      ->hiccup)
  (-> "some text"
      org->
      ->hiccup)
  )

(defn org->hiccup [data]
  (-> data org-> ->hiccup))

(defn markdown->hiccup [data]
  (-> data markdown-> ->hiccup))

(comment

  (org-> "* my header")
  ;; => [{:t "Header",
  ;;      :c
  ;;      [1
  ;;       ["my-header" [] []]
  ;;       [{:t "Str", :c "my"} {:t "Space"} {:t "Str", :c "header"}]]}]

  (org-> "** my header")
  ;; => [{:t "Header",
  ;;      :c
  ;;      [2
  ;;       ["my-header" [] []]
  ;;       [{:t "Str", :c "my"} {:t "Space"} {:t "Str", :c "header"}]]}]
  (org-> "*** my header")
  ;; => [{:t "Header",
  ;;      :c
  ;;      [3
  ;;       ["my-header" [] []]
  ;;       [{:t "Str", :c "my"} {:t "Space"} {:t "Str", :c "header"}]]}]
  )

(comment
  (assert (= (org-> "* head
** subhead
Hello!")
  (markdown-> "# head
## subhead
Hello!")))
  )


(comment
  ;; big example

  ;; org
  ;;
  ;; * okay
  ;;
  ;; - are you okay?
  ;; - anything else?

  ;; from markdown
  (-parse {:source "# okay

- are you okay?
- anything else?"
           :format "markdown"})

  ;; from org
  (-parse {:source "* okay

- are you okay?
- anything else?"
         :format "org"})

  ;; into pandoc
  [{:t "Header", :c [1 ["okay" [] []] [{:t "Str", :c "okay"}]]}
   {:t "BulletList",
    :c
    [[{:t "Plain",
       :c
       [{:t "Str", :c "are"}
        {:t "Space"}
        {:t "Str", :c "you"}
        {:t "Space"}
        {:t "Str", :c "okay?"}]}]
     [{:t "Plain",
       :c [{:t "Str", :c "anything"} {:t "Space"} {:t "Str", :c "else?"}]}]]}]

  (org->hiccup "* okay

- are you okay?
- anything else?")

  ;; into hiccup
  [:div
   [:h1 "okay"]
   [:ul
    [:ol "are you okay?"]
    [:ol "anything else?"]]]

  (org->hiccup "
#+TITLE: Balancing outcome orientation and bottom-up play

* Outcome orientation distributes intent to teams
")

  (org-> "#+TITLE: Balancing\n\n* Outcome orientation")

  (let [source "#+TITLE: Balancing outcome and process\n\n* Outcome orientation"
        format "org"]
    (-> (sh "pandoc" "-f" format "-t" "json" :in source)
        :out
        (json/read-str :key-fn keyword)
        :meta))
  ;; => {:title
  ;;     {:t "MetaInlines",
  ;;      :c
  ;;      [{:t "Str", :c "Balancing"}
  ;;       {:t "Space"}
  ;;       {:t "Str", :c "outcome"}
  ;;       {:t "Space"}
  ;;       {:t "Str", :c "and"}
  ;;       {:t "Space"}
  ;;       {:t "Str", :c "process"}]}}

  )
