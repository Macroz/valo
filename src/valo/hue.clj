(ns valo.hue
  (:require [valo.core :as valo])
  (:require [clj-http.client :as client])
  (:require [cheshire.core :as json])
  (:require [clojure.string :as string])
  (:require [com.evocomputing.colors :as c]))

(defn- hue-request [username password server path & other-params]
  (let [url (str server "/api/" username path)
        _ (println url)
        form-params (first other-params)
        params {;;:basic-auth [username password]
                :content-type :application/json
                :socket-timeout 5000
                :conn-timeout 5000
                :accept :application/json
                :form-params form-params
                :throw-exceptions true}
        response (try (doall (if form-params
                               (client/put url params)
                               (client/get url params)))
                      (catch Throwable t (println t)))
        body (:body response)]
    (when body
      (json/parse-string body keyword))))

(defn- hue-wrapper [data & args]
  (let [{:keys [username password server]} data]
    (apply hue-request username password server args)))

(defprotocol HueSpecific
  (color [this r g b])
  (set-controller [this id])
  (set-scene [this s]))

(defn make-hue []
  (let [data (atom {:white [1.0 1.0 1.0]})]
    (reify
      valo/Core
      (set-server [this url]
        (swap! data assoc :server url))
      (set-user [this username password]
        (swap! data assoc
               :username username
               :password password))
      (reset [this] false)
      (calibrate [this wr wg wb]
        (swap! data assoc :white [wr wg wb]))

      #_valo/Registration
      #_(register-user [this user]
        (let [url (str api)]
          (client/post url (json-request :form-params {"devicetype" "user"
                                                       "username" user}))))

      valo/Discovery
      (get-lights [this]
        (hue-wrapper @data "/lights"))

      valo/Lights
      (set-light [this id r g b]
        (let [v (.color this r g b)]
          (hue-wrapper @data (str "/lights/" id "/state") {:on true :ct 300 :effect :none :bri 254})))

      HueSpecific
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

      (set-scene [this s])
      )))

(def hue (make-hue))
(.set-server hue "http://192.168.0.101")
(.set-user hue "markkurontu" nil)
;;(.get-lights hue)