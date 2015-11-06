(defproject valo "0.1.0-SNAPSHOT"
  :description "valo interactive lighting"
  :url "https://github.com/Macroz/valo"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-http "1.1.0"]
                 [cheshire "5.4.0"]
                 [com.evocomputing/colors "1.0.3"]
                 [stylefruits/gniazdo "0.4.1"]
                 [clj-tagsoup "0.3.0"]
                 [overtone "0.10-SNAPSHOT"]
                 [net.mikera/vectorz-clj "0.29.0"]
                 ;;[net.mikera/imagez "0.5.0"]
                 [hiccup "1.0.5"]
                 ]
  :jvm-opts ["-Xmx2048m"])
