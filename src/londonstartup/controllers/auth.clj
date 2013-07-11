(ns londonstartup.controllers.auth
  (:require [ring.util.response :as resp]
            [londonstartup.common.result :as result]
            [londonstartup.services.twitter :as twitter]
            [londonstartup.models.users :as users]
            [londonstartup.views.auth :as views]
            [londonstartup.services.session :as session]))



(defn callback [uri]
  (str (get (System/getenv) "BASE_URL" (get (System/getenv) "URL")) "/login?auto=true&uri=" uri))

(defn twitter-auth->user [{:keys [screen_name] :as auth}]
  {:username screen_name :auth {:twitter auth}})

(defn find-user [user]
  (let [twitter-id (get-in user [:auth :twitter :user_id ])
        users-result (users/users {"auth.twitter.user_id" twitter-id})]
    (if (not (result/has-error? users-result))
      (first (result/value users-result)))))

(defn update-user! [db-user session-user]
  ; (if (not (= (get-in session-user [:auth :twitter ]) (get-in db-user [:auth :twitter ])))
  (users/update! (merge db-user session-user)))

(defn get-request-token [uri]
  (let [request-token (twitter/request-token (callback uri))]
    (if (not (result/has-error? request-token))
      (result/value request-token))))

(defn get-twitter-auth [request-token oauth_verifier]
  (let [twitter-auth (twitter/access-token-secret request-token oauth_verifier)]
    (if (not (result/has-error? twitter-auth))
      (result/value twitter-auth))))

(defn login [uri oauth_token oauth_verifier denied auto]
  (let [uri (if uri uri "/")]
    (cond
      (session/user-logged?) (resp/redirect uri)
      (not (nil? denied)) (do (session/remove! :request-token ) (session/flash! "ACCESS DENIED") (views/login-page))
      (nil? auto) (views/login-page)
      (or (not oauth_token) (not oauth_verifier)) (if-let [request-token (get-request-token uri)]
                                                    (if (= "true" (:oauth_callback_confirmed request-token))
                                                      (do
                                                        (session/put! :request-token request-token)
                                                        (resp/redirect (result/value (twitter/approval-url request-token))))
                                                      (str "ERROR: oauth_callback_confirmed not true"))
                                                    (str "ERROR: not request-token"))
      :else (let [request-token (session/get! :request-token )]
              (if (= oauth_token (:oauth_token request-token))
                (if-let [twitter_auth (get-twitter-auth request-token oauth_verifier)]
                  (let [session-user (twitter-auth->user twitter_auth)]
                    (session/user-login! session-user)
                    (if-let [db-user (find-user session-user)]
                      (do
                        (update-user! db-user session-user)
                        (resp/redirect uri))
                      (do
                        (users/add! session-user)
                        (resp/redirect "/signup"))))
                  (str "ERROR: cannot get twitter-auth"))
                (str "ERROR: oauth_tokens are different"))))))

(defn logout []
  (session/user-logout!)
  (resp/redirect "/"))