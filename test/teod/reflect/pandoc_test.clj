(ns teod.reflect.pandoc-test
  (:require [teod.reflect.pandoc :as sut]
            [clojure.test :as t]))

(sut/org->hiccup "Paragraph")

(t/deftest org->hiccup-test
  (t/is (= (sut/org->hiccup "Par")
           [:div [:p "Par"]]))
  (t/is (= (sut/org->hiccup "Par 1\n\nPar 2")
           [:div [:p "Par 1"] [:p "Par 2"]]))
  )
