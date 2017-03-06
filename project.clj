(defproject halti-builder "0.1.0-SNAPSHOT"
  :description "Docker image builder for Halti/Funnel"
  :url "http://github.com/emblica"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [http-kit "2.1.18"]
                 [clj-http "3.4.1"]
                 [jarohen/chord "0.8.0"]
                 [clj-jgit "0.8.9"]
                 [me.raynes/conch "0.8.0"]
                 [hikari-cp "1.7.5"]
                 [com.layerware/hugsql "0.4.7"]
                 [org.postgresql/postgresql "9.4.1207"]
                 [compojure "1.5.0"]
                 [com.taoensso/timbre "4.3.1"]
                 [javax.servlet/servlet-api "2.5"]
                 [ring/ring-devel "1.4.0"]
                 [ring/ring-core "1.4.0"]
                 [ring.middleware.logger "0.5.0"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-time "0.11.0"]
                 [cheshire "5.5.0"]
                 [ring/ring-json "0.4.0"]
                 [org.clojure/core.async "0.2.374"]
                 [prismatic/schema "1.1.3"]
                 [org.apache.ant/ant "1.9.4"]]
  :main ^:skip-aot halti-builder.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
