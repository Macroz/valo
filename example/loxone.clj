(ns my.loxone
  (:use [valo.loxone]))

(def MyLoxone (make-loxone))

(.set-server MyLoxone "http://loxone.home.fi")
(.set-user MyLoxone "user" "password")
(.set-controller MyLoxone "0870d46b-033f-0c84-ffffeee00050010b")
(.calibrate MyLoxone 1.0 0.79 0.68)

(.set-scene MyLoxone 8)
(.set-light MyLoxone "AI1" 1.0 0.0 0.5)









