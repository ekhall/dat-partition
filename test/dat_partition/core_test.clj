(ns dat-partition.core-test
  (:require [clojure.test :refer :all]
            [clojure-csv.core :as csv]
            [clojure.java.io :as io]
            [dat-partition.core :refer :all]
            [datomic.api :only (q db) :as d]
            ))

(defn create-empty-db []
  (let [uri "datomic:mem://test-db"]
    ;;(d/delete-database uri)
    (d/create-database uri)
    (let [conn (d/connect uri)
          schema (load-file "resources/schema.edn")]
      @(d/transact conn schema)
      conn)))

(defn test-add-person [lastName]
  (with-redefs [conn (create-empty-db)]
    (do
      (add-person (str lastName "-0"))
      (add-person (str lastName "-1"))
      (d/q '[:find ?lastName
             :where [?e :person/lastName ?lastName]]
           (d/db conn)))))

(test-add-person "Smith")

(defn show-schema []
  (with-redefs [conn (create-empty-db)]
    (do
      (d/q '[:find ?ident
       :where [_ :db/ident ?ident]]
     (d/db conn)))))


(defn test-add-person-with-full [mrn lastName firstName]
  (with-redefs [conn (create-empty-db)]
    (do
      (add-person-with-mrn-and-lastName-firstName mrn lastName firstName)
      (d/q '[:find ?mrn ?lastName ?firstName
             :where
             [?e :person/mrn ?mrn]
             [?e :person/lastName ?lastName]
             [?e :person/firstName ?firstName]]
           (d/db conn)))))

(defn parse-csv-row [row]
  (let [v (first (csv/parse-csv row))]
    (zipmap [:lastName :firstName :dob :mrn :cardiomyopathy :phenotype :gene :lastSeen] v)))

(def test-data "Gonzalez,Isaiah Luis,9/17/2004,MR1261530,DCM,Yes,No,4/24/2014")

(parse-csv-row test-data)

(defn parse-patient-list [list-name]
  (with-open [in-file (io/reader list-name)]
    (doseq [line (line-seq in-file)]
      (parse-csv-row line))))



(parse-patient-list "resources/patients.csv")
(test-add-person "Hercules")
(test-add-person "Xerxes")
(test-add-person-with-full "123" "Smith" "Elmer")

(with-redefs [conn (create-empty-db)]
  (find-all-persons))

(show-schema)
