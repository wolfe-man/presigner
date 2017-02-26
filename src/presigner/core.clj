(ns presigner.core
  (:gen-class
   :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [amazonica.aws.s3 :as s3]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [environ.core :refer [env]]))


(def s3-config
  {:access-key (:access-key env)
   :secret-key (:secret-key env)
   :endpoint (:s3-endpoint env)})


(defmacro with-aws-credentials
  [credentials [aws-func & args]]
  `(let [updated-args# (if (and (:access-key ~credentials)
                                (:secret-key ~credentials))
                         (cons ~credentials (list ~@args))
                         (list ~@args))]
     (apply ~aws-func updated-args#)))


(defn -handleRequest
  [this input output context]
  (let [req (json/read (io/reader input) :key-fn keyword)
        {:keys [bucket obj-key]} req
        presigned-obj (with-aws-credentials s3-config
                        (s3/generate-presigned-url
                         bucket
                         obj-key
                         nil
                         "GET"))]
    (println bucket)
    (println obj-key)
    (println presigned-obj)))
