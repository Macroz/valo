(ns valo.core)

(defprotocol Core
  (set-server [this url])
  (set-user [this username password])
  (reset [this])
  (calibrate [this wr wg wb]))

(defprotocol Registration
  (register-user [this]))

(defprotocol Discovery
  (get-lights [this]))

(defprotocol Lights
  (set-light-hsl [this id h s l])
  (set-light [this id r g b]))
