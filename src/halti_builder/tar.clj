(ns halti-builder.tar
  (:require [clojure.java.io :as io]
            [clojure.core.async :as async :refer :all :exclude [map into reduce merge partition partition-by take]]
            [clojure.string :as string])
  (:import (java.io
              ByteArrayOutputStream
              File
              FileOutputStream
              PipedInputStream
              PipedOutputStream)
           (java.util.zip GZIPOutputStream)
           (org.apache.tools.tar TarEntry TarOutputStream)))

; Source from lein-tar project
; EPL LICENSED
; https://github.com/technomancy/lein-tar
;

(defn unix-path
 "Converts a File or String into a unix-like path"
 [f]
 (-> (.getAbsolutePath f)
     (.replaceAll "\\\\" "/")))

(defn entry-name [cwd f]
  (let [f (unix-path f)]
    (-> f
        (.replaceAll cwd "")
        ;; nuke leading slashes
        (.replaceAll "^\\/" ""))))

(defn- add-file [tar dir f]
  (let [cwd (unix-path dir)
        n (entry-name cwd f)
        entry (doto (TarEntry. f)
                (.setName n))]
    (when-not (empty? n) ;; skip entries with no name
      (when (.canExecute f)
        ;; No way to expose unix perms? you've got to be kidding me, java!
        (.setMode entry 0755))
      (.putNextEntry tar entry)
      (when-not (.isDirectory f)
        (io/copy f tar))
      (.closeEntry tar))))

(defn directory->tar [path]
  (let [dir (clojure.java.io/file path)
        files (chan)
        pipe (PipedInputStream.)]
    (go
      (with-open [tar (TarOutputStream. (PipedOutputStream. pipe))]
        (.setLongFileMode tar TarOutputStream/LONGFILE_GNU)
        (doseq [p (file-seq dir)]
          (>! files (.getPath p))
          (add-file tar dir p))))
    {:out pipe :files files}))
