(defproject cljscale "0.1.0-SNAPSHOT"
  :description "Project that shows how Pulsar is used"
  :url "http://www.deadcoderising.com"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [co.paralleluniverse/pulsar "0.7.3"]
                 [co.paralleluniverse/quasar-core "0.7.3"]]
  :java-agents [[co.paralleluniverse/quasar-core "0.7.3"]]

  :repl-options {:init-ns com.deadcoderising.cljmsg})
