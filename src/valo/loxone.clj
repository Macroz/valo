(ns valo.loxone
  (:require [valo.core :as valo])
  (:require [clj-http.client :as client])
  (:require [cheshire.core :as json])
  (:require [clojure.string :as string])
  (:require [pl.danieljanus.tagsoup :as soup])
  (:require [com.evocomputing.colors :as c]))

(defn- loxone-request [username password server path]
  (let [url (str server path)
        params {:basic-auth [username password]
                :content-type :application/json
                :socket-timeout 5000
                :conn-timeout 5000
                :accept :application/json
                :throw-exceptions false}
        response (try (doall (client/get url params)) (catch Throwable t))
        body (:body response)]
    (when body
      (soup/parse-string body))))

(defn- loxone-wrapper [data & args]
  (let [{:keys [username password server]} data]
    (apply loxone-request username password server args)))

(defn- parse-input [s]
  (map second (re-seq #"[^\(\)]+\(([^,\)]+)[^\)]+\)" s)))

(defprotocol LoxoneSpecific
  (color [this r g b])
  (set-controller [this id])
  (set-scene [this s]))

(defn make-loxone []
  (let [data (atom {:white [1.0 1.0 1.0]})]
    (reify
      valo/Core
      (set-server [this url]
        (swap! data assoc :server url))
      (set-user [this username password]
        (swap! data assoc
               :username username
               :password password))
      (reset [this]
        (loxone-wrapper @data "/dev/sps/restart"))
      (calibrate [this wr wg wb]
        (swap! data assoc :white [wr wg wb]))

      valo/Discovery
      (get-lights [this]
        ;; incomplete implementation
        (parse-input (:value (second (loxone-wrapper @data "/dev/sps/enumin")))))

      valo/Lights
      (set-light [this id r g b]
        (let [v (.color this r g b)]
          (loxone-wrapper @data (str "/dev/sps/io/" (@data :controller) "/" id "/" v))))

      LoxoneSpecific
      (color [this r g b]
        (let [[wr wg wb] (@data :white)
              r (* r wr)
              g (* g wg)
              b (* b wb)
              r (int (* r 100))
              g (int (* g 100))
              b (int (* b 100))]
          (+ (* 1000000 b) (* 1000 g) r)))

      (set-controller [this id]
        (swap! data assoc :controller id))

      (set-scene [this s]
        (loxone-wrapper @data (str "/dev/sps/io/" (@data :controller) "/" s)))
      )))
