(ns londonstartup.handler-test
  (:require [londonstartup.controllers.startups :as startups])
  (import org.bson.types.ObjectId)
  (:use midje.sweet)
  (:use [midje.util :only [expose-testables]]))

(expose-testables londonstartup.controllers.auth)