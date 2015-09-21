(ns com.deadcoderising.cljmsg
  (:require
   [co.paralleluniverse.pulsar
    [core :refer :all]
    [actors :refer :all]])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(defn- get-name [ref clients]
  (-> (first (filter #(= (:ref %) ref) clients))
      :name))

(defn- broadcast [msg clients]
  (doseq [c clients]
    (! (:ref c) msg)))

(defsfn server [clients]
  (receive
   [:join ref name] (do
                      (link! ref)
                      (broadcast
                       [:info (str name " joined the chat")]
                       clients)
                      (recur (conj clients {:name name :ref ref})))
   [:send ref msg]  (do
                      (broadcast
                       [:new-msg (get-name ref clients) msg]
                       clients)
                      (recur clients))
   :shutdown        (println "Shutting down")
   [:exit _ ref _]  (do
                      (broadcast
                       [:info (str (get-name ref clients) " left the chat")]
                       clients)
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
   [:exit _ _ _] (client-prn name "Lost connection. Shutting down...")))

(defn create-client [name server]
  (let [c (spawn :trap true client name server)]
    (! server [:join c name])
    c))

