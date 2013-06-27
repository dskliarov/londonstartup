(ns londonstartup.views.startups
  (:require [noir.validation :as validate]
            [clojure.string :as string]
            [londonstartup.views.common :as common]
            [londonstartup.views.bootstrap :as bs])
  (:use hiccup.core
        hiccup.element
        hiccup.page
        hiccup.form
        hiccup.def))

;;Validation
;(defn valid? [{:keys [website name]}]
;  (validate/rule (validate/has-value? website)
;    [:website "A startup must have a website"])
;  (validate/rule (validate/has-value? name)
;    [:name "A startup must have a name"])
;  ;(not (validate/errors? :website :name)) ;;TODO: valid? should be at view level.
;  )

;; Page Elements

(defn id [{:keys [_id]} & suffix]
  (let [prefix (str "startup-" _id)
        suffix (first suffix)]
    (if suffix
      (str prefix "-" suffix)
      prefix)))

(defn idref [startup & suffix]
  (str "#" (id startup (first suffix))))

(defn get-link [{:keys [website]} & content]
  (link-to (str "/startups/" website) content))

(defn update-link [{:keys [website]} & content]
  (link-to (str "/update/startups/" website) content))

;;Forms
(defn error-text [errors]
  [:span (string/join "" errors)])

(defn startup-fields [{:keys [website name accountId phone addressLine1 addressLine2 addressLine3 city county country postCode _id]}]
  ;(validate/on-error :name error-text)
  (list
    [:div.row-fluid [:div.span6 [:div.control-group (label {:class "control-label"} :name "Name")
                                  [:div.controls (text-field {:placeholder "Name"} :name name)]]

                      [:div.control-group (label {:class "control-label"} :website "Website")
                       [:div.controls (text-field {:placeholder "Website"} :website website)]]

                      [:div.control-group (label {:class "control-label"} :accountId "Twitter")
                       [:div.controls (text-field {:placeholder "Twitter Account"} :accountId accountId)]]

                      [:div.control-group (label {:class "control-label"} :phone "Phone")
                       [:div.controls (text-field {:placeholder "ex: +44 20 7123 1234"} :phone phone)]]

                      ]
     [:div.span6 [:div.control-group (label {:class "control-label"} :address "Address")
                  [:div.controls (text-field {:placeholder "Address Line 1"} :addressLine1 addressLine1) [:br ]
                   (text-field {:placeholder "Address Line 2"} :addressLine2 addressLine2) [:br ]
                   (text-field {:placeholder "Address Line 3"} :addressLine3 addressLine3)
                   ]
                  ]
      [:div.control-group (label {:class "control-label"} :city "City")
       [:div.controls (text-field {:placeholder "ex: London"} :city city)]]
      [:div.control-group (label {:class "control-label"} :county "County")
       [:div.controls (text-field {:placeholder "ex: Greater London"} :county county)]]
      [:div.control-group (label {:class "control-label"} :postCode "Post Code")
       [:div.controls (text-field {:placeholder "ex: SW1W 9XX"} :postCode postCode)]]
      [:div.control-group (label {:class "control-label"} :country "Country")
       [:div.controls (text-field {:placeholder "ex: UK"} :country country)]]
      ]
     (hidden-field :_id _id)
     ]

    ))

(defn startup-remove-form [{:keys [website]}]
  (when website
    (form-to [:delete (str "/startups/" website)] ;The url should be calculated from the route
      (submit-button {:class "btn btn-danger"} "Delete"))))

(defn startup-form [action method url startup]
  (form-to {:class "form-horizontal"} [method url]
    (startup-fields startup)
    [:div.row-fluid [:div.span4 [:a.btn {:href url} "Cancel"]
                      (submit-button {:class "btn btn-primary"} action)]]
    ))

;;Header
(defn badge [icon value]
  [:small (bs/icon icon) " " value " "])

(defn startup-edit-button [startup]
  [:small (update-link startup [:i.icon-edit ])])

(defn startup-name [{:keys [name] :as startup}]
  [:div.startup-header.span6 [:h2 (get-link startup name) [:small (update-link startup [:i.icon-edit ])]]])



(defn startup-badges [startup]
  [:div.startup-badges.span4.offset2 [:h2 (badge "icon-user" 13) (badge "icon-bullhorn" 2450) (badge "icon-heart" 200)]])

(defn startup-header [startup]
  [:div.row (startup-name startup) (startup-badges startup)])


;;Details
(defn address [{:keys [website name accountId phone addressLine1 addressLine2 addressLine3 city county country postCode _id]}]
  [:address.span3 [:strong "Address"] [:br ]
   [:span addressLine1] [:br ]
   [:span addressLine2] [:br ]
   [:span addressLine3] [:br ]
   [:span city] [:br ]
   [:span county] [:br ]
   [:span country] [:br ]
   [:span postCode] [:br ] [:br ]

   [:strong "Phone"] [:br ]
   [:span phone] [:br ] [:br ]
   [:strong "Website"] [:br ]
   (let [domain-name (string/replace website "http://" "")]
     (link-to (str "http://" domain-name) domain-name))
   ]
  )

;;Details
(defn location [{:keys [name website] :as startup}]
  (str "<div class=\"span3\">
                      <iframe width=\"300\" height=\"300\" frameborder=\"0\" scrolling=\"no\" marginheight=\"0\" marginwidth=\"0\"
                              src=\"https://maps.google.co.uk/maps?f=q&amp;source=s_q&amp;hl=en&amp;geocode=&amp;q=Google+London,+Buckingham+Palace+Road,+London&amp;aq=0&amp;oq=google+lon&amp;sll=51.48931,-0.08819&amp;sspn=1.061176,1.253815&amp;ie=UTF8&amp;hq=Google+London,+Buckingham+Palace+Road,+London&amp;t=m&amp;ll=51.494904,-0.146427&amp;spn=0.008016,0.012875&amp;z=15&amp;iwloc=near&amp;output=embed\"></iframe>
                      <br/>
                      <small><a
                              href=\"https://maps.google.co.uk/maps?f=q&amp;source=embed&amp;hl=en&amp;geocode=&amp;q=Google+London,+Buckingham+Palace+Road,+London&amp;aq=0&amp;oq=google+lon&amp;sll=51.48931,-0.08819&amp;sspn=1.061176,1.253815&amp;ie=UTF8&amp;hq=Google+London,+Buckingham+Palace+Road,+London&amp;t=m&amp;ll=51.494904,-0.146427&amp;spn=0.008016,0.012875&amp;z=15&amp;iwloc=A\"
                              >View Larger Map</a></small>
                  </div>")
  )

;;One startup
(defn startup-summary [{:keys [_id website name] :as startup}]
  (when startup
    [:section.startup.container-fluid {:id (id startup)}
     (startup-header startup)]))

(defn startup-dashboard [startup]
  (when startup
    [:section.startup.container-fluid {:id (id startup)}
     (startup-header startup)
     [:div.row (address startup)
      (location startup)
      ]]
    ))

;;Several startups
(defn startup-list [startups]
  (map #(startup-summary %) startups))

;; Pages
(defn startup-page [{:keys [website] :as startup}]
  (common/layout
    (startup-dashboard startup))) ;The url should be calculated from the route

(defn startups-page [startups query]
  (common/layout
    {:navbar {:search {:query query}}}
    (startup-list startups)))

;;Form Pages

(defn add-startup-page []
  (common/layout
    {:navbar {:search {:enabled false}}}
    [:header.jumbotron.subhead [:div.container [:h1 "Add New Startup"]]]
    [:div.container-fluid [:div.row-fluid [:div.span12 (startup-form "Add" :post "/startups" {})]]])) ;The url should be calculated from the route

(defn update-startup-page [{:keys [website] :as startup}]
  (common/layout
    [:header.container-fluid (startup-header startup)]
    [:div.container-fluid [:div.row-fluid [:div.span12 (startup-form "Update" :put (str "/startups/" website) startup) ;The url should be calculated from the route
                                           (startup-remove-form startup)]]]))