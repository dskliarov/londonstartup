(ns londonstartup.controllers.startups
  (:require [ring.util.response :as resp]
            [londonstartup.models.startup :as startup]
            [londonstartup.views.startups :as views]
            [londonstartup.common.result :as result])
  (import org.bson.types.ObjectId))

(defn adapt-startup [startup]
  (if-let [_id (:_id startup)]
    (if (instance? ObjectId _id)
      startup
      (try
        (assoc startup :_id (ObjectId. _id))
        (catch Exception e (dissoc startup :_id))))
    startup))


;;Read
(defn startups [query]
  (views/startups-page (startup/startups) query))

(defn startup [website]
  (let [lookup-result (startup/website->startup website)]
    (if (not (result/has-error? lookup-result))
      (views/startup-page lookup-result)
      (resp/redirect "/startups"))))

;;Modify
(defn startup-new [startup]
  (let [startup (adapt-startup startup)
        add-result (startup/add! startup)]
    (if (not (result/has-error? add-result))
      (resp/redirect-after-post (str "/startups/" (:website startup)))
      (views/add-startup-page add-result))))

(defn startup-update [startup & [default-website]]
  (let [startup (adapt-startup startup)
        update-result (startup/update! startup)]
    (if (not (result/has-error? update-result))
      (resp/redirect-after-post (str "/startups/" (:website startup)))
      (views/update-startup-page update-result default-website))))

(defn startup-delete [website]
  (startup/remove-website! website)
  (resp/redirect "/startups"))

;;Forms
(defn add-startup-form []
  (views/add-startup-page (result/result {})))

(defn update-startup-form [website]
  (let [lookup-result (startup/website->startup website)]
    (if (not (result/has-error? lookup-result))
      (views/update-startup-page lookup-result)
      (resp/redirect "/startups"))))