(ns teod.reflect.filewriter
  "A builder takes EDN and produces an output file.

  An EDN source files pick its writer with metadata."
  (:require [teod.reflect.info :as info]
            [hiccup2.core]
            [clojure.string :as str]))

(defmulti write #'info/filewriter)

(defmethod write :teod.reflect.filewriter/hiccup-html [edn]
  (let [edn-path (info/source-path edn)
        html (hiccup2.core/html edn)
        html-path (str/replace edn-path #"\.edn$" ".html")]
    (assert (not= edn-path html-path) "Conflict: same input and output path.")
    (spit html-path
          (str "<!doctype html>"
               "\n"
               html))))

(defmethod write :default [edn]
  (println "no builder registered for" (info/filewriter edn))
  (println " => no action taken."))

(comment
  ;; multimethods can be a hassle in the REPL - just onload the whole ns.
  (remove-ns (-> *ns* str symbol))

  (str/replace "index.edn" #"\.edn$" ".html")
  )
