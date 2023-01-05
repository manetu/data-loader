;; Copyright © 2020-2022 Manetu, Inc.  All rights reserved

(ns manetu.data-loader.sparql
  (:require [clostache.parser :as clostache]))

(def template
  "
   PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>
   PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
   PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>
   PREFIX person: <http://www.w3.org/ns/person#>
   PREFIX manetu: <http://manetu.com/manetu/>
   PREFIX vid:    <http://manetu.io/rdf/vaultid/0.1/>

   INSERT
   {
          _:r rdfs:Class manetu:Root ;
              manetu:email \"{{email}}\" .
   }
   WHERE
   {
          FILTER NOT EXISTS { ?r rdfs:Class manetu:Root . }
   };

   INSERT
   {
          ?r    manetu:hasSource _:s .
          _:s   rdfs:Class manetu:Source ;
                manetu:type \"{{type}}\" ;
                manetu:id \"{{id}}\" ;
                manetu:schemaClass \"{{class}}\" ;
                manetu:hasPerson _:p .
   }
   WHERE
   {
          ?r    rdfs:Class manetu:Root .
          FILTER NOT EXISTS {
              ?r    manetu:hasSource ?s .
              ?s    manetu:id \"{{id}}\" ;
                    rdfs:Class manetu:Source .
          }
   };

   DELETE { ?p ?a ?v }
   WHERE
   {
          ?s   manetu:id \"{{id}}\" ;
               rdfs:Class manetu:Source ;
               manetu:hasPerson ?p .
          ?p ?a ?v .
   };

   INSERT
   {
          ?p   rdfs:Class person:Person .
          {{#attributes}}
          ?p {{{name}}} {{{value}}} .
          {{/attributes}}
   }
   WHERE
   {
          ?s   manetu:id \"{{id}}\" ;
               rdfs:Class manetu:Source ;
               manetu:hasPerson ?p .
   };

   DELETE { ?e ?a ?v }
   WHERE
   {
          ?e    rdfs:Class vid:Descriptor ;
                ?a ?v .
   };

   INSERT {
          _:t    rdfs:Class       vid:Descriptor ;
                 vid:Description  \"Data Subject Email\" ;
                 vid:Value        ?email .
   }
   WHERE {
          ?r     rdfs:Class    manetu:Root .
          ?email rdf:subject   ?r ;
                 rdf:predicate manetu:email .
   };

  ")

(def detectable-types
  [[#"^[+-]?\d+(\.\d+)?[eE][-+]?[0-9]*$"                     "double"]
   [#"^[+-]?\d+\.\d+?$"                                      "decimal"]
   [#"^[+-]?\d+$"                                            "integer"]
   [#"^\d{4}-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])Z?$"  "date"]])

(defn detect-type [v]
  (or (some (fn [[re type]]
              (when (some? (re-matches re v))
                type))
            detectable-types)
      "string"))

(defn field-> [[k v]] {:name (str "person:" (name k)) :value (str "\"" v "\"^^xsd:" (detect-type v))})

(defn convert
  [{:keys [type id class] :as options} {:keys [Email] :as record}]
  (clostache/render template {:type type :id id :class class :email Email :attributes (map field-> record)}))
