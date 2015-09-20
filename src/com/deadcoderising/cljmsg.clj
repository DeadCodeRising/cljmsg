(ns com.deadcoderising.cljmsg
  (:require
   [co.paralleluniverse.pulsar
    [core :refer :all]
    [actors :refer :all]])
  (:refer-clojure :exclude [promise await])
  (:gen-class))


(defn- broadcast [msg clients]
  (doseq [c clients]
    (! (:ref c) msg)))

(defn- get-client [ref clients]
  (first (filter #(= (:ref %) ref) clients)))

(defn- get-name [ref clients]
  (:name (get-client ref clients)))

(defn- add-client [name ref clients]
  (conj clients {:name name :ref ref}))

(defn- broadcast-join [name clients]
  (broadcast [:info (str name " joined the chat")]
             clients))

(defn- broadcast-msg [sender msg clients]
  (broadcast [:new-msg (get-name ref clients) msg]
             clients))

(defn- broadcast-leave [ref clients]
  (broadcast [:info (str (get-name ref clients)
                         " left the chat")]
             clients))

(defsfn server [clients]
  (receive
   [:join ref name] (do
                      (link! ref)
                      (broadcast-join name clients)
                      (recur (add-client name ref clients)))
   [:send ref msg] (do
                     (broadcast-msg ref msg clients)
                     (recur clients))
   :shutdown       (println "Shutting down")
   [:exit _ ref _] (do
                     (broadcast-leave ref clients)
                     (recur clients))))

(defn create-server []
  (spawn :trap true server '()))

(defn- client-prn [name msg]
  (println (format "[%s's client] - %s" name msg)))

(defn- prn-msg [name from msg]
  (client-prn name (format "%s: %s" from msg)))

(defsfn client [name server]
  (receive
   [:new-msg from msg] (do (prn-msg name from msg)
                           (recur name server)) 
   [:info msg] (do
                 (client-prn name msg)
                 (recur name server))
   [:send msg] (do
                 (! server [:send @self msg])
                 (recur name server))
   :disconnect (client-prn name "Disconnected")
   [:exit _ _ _] (client-prn name "Lost connection")))

(defn create-client [name server]
  (let [c (spawn :trap true client name server)]
    (! server [:join c name])
    c))

