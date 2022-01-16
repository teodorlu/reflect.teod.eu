(ns teod.reflect.info)

;; information protocol for EDN object metadata on teod.reflect

(comment
  ;; Example of metadata-enriched EDN
  ^{:teod.reflect/source-path "teod.edn"
    :teod.reflect/transformers ['teod.reflect.sci/eval]
    :teod.reflect/filewriter :teod.reflect.filewriter/hiccup-html}
  [:html
   [:name "Teodor"]
   [:age (+ 1 2 3 25)]]

  ;; The site generator can process that EDN to this HTML, written to the file
  ;; "teod.html":
  ;;
  ;; <html>
  ;;   <name>Teodor</html>
  ;;   <age>31</age>
  ;; </html>

  )

(defn source-path [edn]
  (:teod.reflect/source-path (meta edn)))

(defn set-source-path [edn source-path]
  (vary-meta edn assoc :teod.reflect/source-path source-path))

(defn transformers [edn]
  (:teod.reflect/transformers (meta edn)
                              []))

(defn set-transformers [edn transformers]
  (vary-meta edn assoc :teod.reflect/transformers transformers))

(defn filewriter [edn]
  (:teod.reflect/filewriter (meta edn)))

(defn set-filewriter [edn filewriter]
  (vary-meta edn assoc :teod.reflect/filewriter filewriter))
