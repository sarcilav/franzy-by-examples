(defproject example "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-kafka.franzy/core "2.0.7"]
                 [clj-kafka.franzy/common "2.0.7"]
                 [clj-kafka.franzy/nippy "2.0.7"]
                 [clj-kafka.franzy/json "2.0.7"]
                 [clj-kafka.franzy/examples "2.0.7"]]
  :main ^:skip-aot example.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
