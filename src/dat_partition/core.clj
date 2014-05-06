(ns dat-partition.core
  (:require [datomic.api :as d]))

(def conn nil)

(defn add-person [lastName]
  @(d/transact conn [{:db/id (d/tempid :person)
                      :person/lastName lastName}]))

(defn add-person-with-mrn-and-lastName-firstName [mrn lastName firstName]
  @(d/transact conn [{:db/id (d/tempid :person)
                      :person/mrn mrn
                      :person/lastName lastName
                      :person/firstName firstName}]))

(defn find-all-persons []
  (d/q '[:find ?mrn ?lastName ?firstName
             :where
             [_ :person/mrn ?mrn]
             [?e :person/lastName ?lastName]
             [_ :person/firstName ?firstName]]
       (d/db conn)))
