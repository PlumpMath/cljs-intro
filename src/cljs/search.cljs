(ns cljs-intro.search
	(:require 
			[domina :as d]
			[domina.xpath :as dx]
		  [clojure.browser.event :as event]
			[goog.net.XhrIo :as jsonp]
			[shoreleave.remotes.request :as sl-req]
			[cljs-intro.views :as v]
			[cljs-intro.pubsub :as ps]))
			
; Grab a reference to the search button so we can
; hook up an event handler to it.
(def ^{:doc "Reference to the search-button DOM object" }
   search-button (d/by-id "search-btn"))
(def lastname-field (d/by-id "lname"))
(def history-button (d/by-id "history-btn"))

(d/set-styles! history-button {:display "none"})

(defn update-results-div [data]
	(let [res-div (dx/xpath "//div[@id='results']")]
	  (d/destroy-children! res-div)
	  (d/append! res-div data)))

(defn ^{:doc "converts the returned JSON to clj data, removes old html in the
              results div and calls the function to display the new stats"} 
	display-results [data]
 (update-results-div (v/show-stats data)))

(defn sl-display-results [{:keys [body event]}]
	(update-results-div (v/show-stats (js->clj body :keywordize-keys true))))

(defn find-player [lastname]
	(sl-req/request (str "/player/" lastname)
			:on-success sl-display-results
			:on-error #(js/alert "an error!")
			:use-json true))

(defn search-state-change [{:keys [new]}]
	(if (> (count (:previous-searches new)) 0)
	   (d/set-styles! history-button {:display ""})))

(defn view-history [current-lname]
	(update-results-div (v/show-history (conj (:previous-searches @ps/search-state) current-lname))))

; Subscriptions
(ps/subscribe-to :search find-player)
(ps/subscribe-to :results display-results)
(ps/subscribe-to ps/search-state search-state-change)
(ps/subscribe-to :history view-history)

; Event handlers
(event/listen search-button "click"
 (fn [] (ps/publish-search-string (d/value lastname-field))))

(event/listen history-button "click" (fn [] (ps/publish-view-history (d/value lastname-field))))