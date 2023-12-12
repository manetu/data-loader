;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.data-loader.driver.drivers.grpc.login
  (:require [protojure.grpc.client.providers.http2 :as grpc.http2]
            [protojure.protobuf.any :refer [any->]]
            [clojure.core.async :refer [go-loop <!] :as async]
            [manetu.api.idp.v1.API.client :as idp.client]
            [manetu.data-loader.utils :as utils]
            [promesa.core :as p]
            [taoensso.timbre :as log])
  (:refer-clojure :exclude [resolve]))

(defn- wait-for-token
  [ch]
  (log/debug "waiting for approval")
  (p/create
   (fn [resolve reject]
     (go-loop []
       (let [e (<! ch)]
         (if (nil? e)
           (reject (ex-info "No token found" {}))
           (if-let [token (:auth-token (any-> (:value e)))]
             (resolve token)
             (recur))))))))

(defn- web-login
  "Acquires a real JWT from the Manetu IDP"
  [{:keys [url insecure userid password provider] :as ctx}]
  (let [ch (async/chan 16)
        params {:header (utils/create-header)
                :idp-creds {:email userid :password password}
                :oidc {:client-id provider}}]
    (log/debug "logging in")
    (-> (grpc.http2/connect {:uri url :insecure? insecure})
        (p/then (fn [client] (idp.client/WebLogin client params ch)))
        (p/then (fn [_] (wait-for-token ch)))
        (p/then (fn [token]
                  (log/debug "login successful:" token)
                  token))
        (p/catch (fn [e]
                   (log/error "login failed:" e)
                   (throw e))))))

(defn connect
  [{:keys [url insecure] :as options}]
  (log/debug "connecting")
  (-> (web-login options)
      (p/then
       (fn [token]
         (grpc.http2/connect (cond-> {:uri url :metadata {"authorization" (str "bearer " token)}}
                               (true? insecure) (assoc :insecure? true)))))
      (p/then
       (fn [client]
         (log/debug "connected")
         client))))
