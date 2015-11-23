(ns valo.hue
  (:require [valo.core :as valo])
  (:require [clj-http.client :as client])
  (:require [cheshire.core :as json])
  (:require [clojure.string :as string])
  (:require [com.evocomputing.colors :as c]))

(defn- hue-request [username password server path & other-params]
  (let [url (str server "/api/" username path)
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

(defn- rgb->hsl [r g b]
  (:hsl (c/create-color {:r (int (* r 255.0)) :g (int (* 255.0 g)) :b (int (* 255.0 b))})))

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
        (let [[h s l] (rgb->hsl r g b)]
          (.set-light-hsl this id h s l)))
      (set-light-hsl [this id h s l]
        (let [hue (int (* (/ (mod h 360.0) 360.0) 65535.0))
              sat (int (* s 0.01 255.0))
              bri (int (+ (* l 2.53) 1.0))]
            (if (<= l 0)
              (hue-wrapper @data (str "/lights/" id "/state") {:on false})
              (hue-wrapper @data (str "/lights/" id "/state") {:on true :bri bri :hue hue :sat sat})))))

      HueSpecific
      ;; (color-hsl [this h s l]
      ;;   (let [[r g b] (map #(/ % 255.0) (take 3 (:rgba (c/create-color {:h h :s s :l l}))))]
      ;;     (.color this r g b)))
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
;;(.set-light hue 6 1.0 0.0 0.0)
