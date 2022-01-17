(ns teod.reflect.pandoc-test
  (:require [teod.reflect.pandoc :as sut]
            [clojure.test :as t]))

(t/deftest org->hiccup-test
  (t/are [pandoc hiccup] (= (sut/->hiccup* pandoc {})
                            hiccup)
    {:t "Para", :c [{:t "Str", :c "Paragraph"}]}
    [:p "Paragraph"]

    {:t "Space"}
    " "

    {:t "Str", :c "A plain string"}
    "A plain string"

    {:t "Emph", :c [{:t "Str", :c "two"}]}
    [:em "two"]
    ))
