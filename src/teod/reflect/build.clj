(ns teod.reflect.build
  (:require
   [clojure.edn :as edn]
   [clojure.stacktrace]
   [hawk.core :as hawk]
   [teod.reflect.filewriter :as writer]
   [teod.reflect.info :as info]
   [teod.reflect.transformers :as transformers]))

(defn index-edn? [ctx {:keys [file] :as e}]
  (and (hawk/file? ctx e)
       (#{"index.edn" #_ "simple.edn"}
          (.getName file))))

(defn watch-rebuild-edn-handler
  [_ctx e]
  (let [{:keys [_kind file]} e]
    (try
      (let [edn-path (.getPath file)
            _ (print "building" edn-path "...")
            edn (-> edn-path
                    slurp
                    edn/read-string
                    (info/set-source-path edn-path)
                    transformers/transform)]
        (writer/write edn)
        (println " done."))
      (catch Throwable t
        (println "Faied!")
        (clojure.stacktrace/print-stack-trace t)
        (tap> t)))))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn watch!
  "Look for changes to EDN files; then try to rebuild.

  Called directly from build system.

  Accepts emtpy map to conform to clojure -X conventions."
  [_opts]
  (println "Watching and rebuilding index.edn files")
  (hawk/watch! [{:paths ["."]
                 :filter #'index-edn?
                 :handler #'watch-rebuild-edn-handler}]))

(defn stop! [watcher]
  (hawk/stop! watcher))
