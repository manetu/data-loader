;; Copyright © 2020-2022 Manetu, Inc.  All rights reserved

(ns manetu.data-loader.sparql
  (:require [clostache.parser :as clostache]))

(def template
  "
   PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
   PREFIX person: <http://www.w3.org/ns/person#>
   PREFIX manetu: <http://manetu.com/manetu/>

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
   }

  ")

(defn field-> [[k v]] {:name (str "person:" (name k)) :value (str "\"" v "\"")})

(defn convert
  [{:keys [type id class] :as options} {:keys [Email] :as record}]
  (clostache/render template {:type type :id id :class class :email Email :attributes (map field-> record)}))
