(ns teod.reflect.pandoc-test
  (:require [teod.reflect.pandoc :as sut]
            [clojure.test :as t]))

(t/deftest org->hiccup-test
  ;; Are er skikkelig fin -- den lar oss skrive par med input og output, uten å
  ;; styre med is, = funksjonskall og sånn. Anbefalt!
  (t/are [pandoc hiccup] (= hiccup
                            (sut/->hiccup* pandoc {}))
    {:t "Para", :c [{:t "Str", :c "Paragraph"}]}
    [:p "Paragraph"]

    {:t "Space"}
    " "

    {:t "Str", :c "A plain string"}
    "A plain string"

    {:t "Emph", :c [{:t "Str", :c "two"}]}
    [:em "two"]

    {:t "Para",
     :c
     [{:t "Str", :c "Para"}
      {:t "Space"}
      {:t "Emph", :c [{:t "Str", :c "two"}]}
      {:t "Str", :c "."}]}
    [:p "Para" " " [:em "two"] "."]

    {:t "BulletList",
     :c
     [[{:t "Plain", :c [{:t "Str", :c "Item"}]}]
      [{:t "Plain",
        :c [{:t "Str", :c "Another"} {:t "Space"} {:t "Str", :c "item"}]}]]}
    [:ul
     [:li [:span "Item"]]
     [:li [:span "Another" " " "item"]]]
    ))
