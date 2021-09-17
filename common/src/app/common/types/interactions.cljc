;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) UXBOX Labs SL

(ns app.common.types.interactions
  (:require
   [app.common.geom.point :as gpt]
   [app.common.spec :as us]
   [clojure.spec.alpha :as s]))

(s/def ::point
  (s/and (s/keys :req-un [::x ::y])
         gpt/point?))

;; -- Options depending on event type

(s/def ::event-type #{:click
                      :mouse-over
                      :mouse-press
                      :mouse-enter
                      :mouse-leave
                      :after-delay})

(s/def ::delay ::us/safe-integer)

(defmulti event-opts-spec :event-type)

(defmethod event-opts-spec :after-delay [_]
  (s/keys :req-un [::delay]))

(defmethod event-opts-spec :default [_]
  (s/keys :req-un []))

(s/def ::event-opts
  (s/multi-spec event-opts-spec ::event-type))

;; -- Options depending on action type

(s/def ::action-type #{:navigate
                       :open-overlay
                       :close-overlay
                       :prev-screen
                       :open-url})

(s/def ::destination (s/nilable ::us/uuid))
(s/def ::overlay-pos-type #{:manual
                            :center
                            :top-left
                            :top-right
                            :top-center
                            :bottom-left
                            :bottom-right
                            :bottom-center})
(s/def ::overlay-position ::point)
(s/def ::url ::us/string)

(defmulti action-opts-spec :action-type)

(defmethod action-opts-spec :navigate [_]
  (s/keys :req-un [::destination]))

(defmethod action-opts-spec :open-overlay [_]
  (s/keys :req-un [::destination
                   ::overlay-position
                   ::overlay-pos-type]))

(defmethod action-opts-spec :close-overlay [_]
  (s/keys :req-un [::destination]))

(defmethod action-opts-spec :prev-screen [_]
  (s/keys :req-un []))

(defmethod action-opts-spec :open-url [_]
  (s/keys :req-un [::url]))

(s/def ::action-opts
  (s/multi-spec action-opts-spec ::action-type))

;; -- Interaction

(s/def ::classifier
  (s/keys :req-un [::event-type
                   ::action-type]))

(s/def ::interaction
  (s/merge ::classifier
           ::event-opts
           ::action-opts))

(s/def ::interactions
  (s/coll-of ::interaction :kind vector?))

(def default-interaction
  {:event-type :click
   :action-type :navigate
   :destination nil})

(def default-delay 100)

;; -- Helpers

(defn set-event-type
  [interaction event-type]
  (us/verify ::interaction interaction)
  (us/verify ::event-type event-type)
  (if (= (:event-type interaction) event-type)
    interaction
    (case event-type

      :after-delay
      (assoc interaction
             :event-type event-type
             :delay (get interaction :delay default-delay))

      (assoc interaction
             :event-type event-type))))


(defn set-action-type
  [interaction action-type]
  (us/verify ::interaction interaction)
  (us/verify ::action-type action-type)
  (if (= (:action-type interaction) action-type)
    interaction
    (case action-type

      :navigate
      (assoc interaction
             :action-type action-type
             :destination (get interaction :destination))

      :open-overlay
      (assoc interaction
             :action-type action-type
             :destination (get interaction :destination)
             :overlay-pos-type (get interaction :overlay-pos-type :center)
             :overlay-position (get interaction :overlay-position (gpt/point 0 0)))

      :close-overlay
      (assoc interaction
             :action-type action-type
             :destination (get interaction :destination))

      :prev-screen
      (assoc interaction
             :action-type action-type)

      :open-url
      (assoc interaction
             :action-type action-type
             :url (get interaction :url "")))))


(declare calc-overlay-position)

(defn set-destination
  [interaction destination shape objects]
  (us/verify ::interaction interaction)
  (us/verify ::destination destination)
  (assert (or (nil? destination)
              (some? (get objects destination))))
  (assert #(:navigate :open-overlay :close-overlay) (:action-type interaction))
  (cond-> interaction
    :always
    (assoc :destination destination
           )
    (= (:action-type interaction) :open-overlay)
    (assoc :overlay-pos-type :center
           :overlay-position (calc-overlay-position destination
                                                    shape
                                                    objects
                                                    :center))))

(defn set-overlay-pos-type
  [interaction overlay-pos-type shape objects]
  (us/verify ::interaction interaction)
  (us/verify ::overlay-pos-type overlay-pos-type)
  (assert #(= :open-overlay (:action-type interaction)))
  (let [destination (->> interaction
                         :destination
                         (get objects))]
    (assoc interaction
           :overlay-pos-type overlay-pos-type
           :overlay-position (calc-overlay-position destination
                                                    shape
                                                    objects
                                                    overlay-pos-type))))

(defn toggle-overlay-pos-type
  [interaction overlay-pos-type shape objects]
  (us/verify ::interaction interaction)
  (us/verify ::overlay-pos-type overlay-pos-type)
  (assert #(= :open-overlay (:action-type interaction)))
  (let [destination (->> interaction
                         :destination
                         (get objects))
        new-pos-type (if (= (:overlay-pos-type interaction) overlay-pos-type)
                       :manual
                       overlay-pos-type)]
    (assoc interaction
           :overlay-pos-type new-pos-type
           :overlay-position (calc-overlay-position destination
                                                    shape
                                                    objects
                                                    new-pos-type))))

(defn set-overlay-position
  [interaction overlay-position]
  (us/verify ::interaction interaction)
  (us/verify ::overlay-position overlay-position)
  (assert #(= :open-overlay (:action-type interaction)))
  (assoc interaction :overlay-position overlay-position))

(defn calc-overlay-position
  [destination shape objects overlay-pos-type]
  (if (nil? destination)
    (gpt/point 0 0)
    (let [dest-frame   (get objects destination)
          overlay-size (:selrect dest-frame)
          orig-frame   (if (= (:type shape) :frame)
                         shape
                         (get objects (:frame-id shape)))
          frame-size   (:selrect orig-frame)]
      (case overlay-pos-type

        :center
        (gpt/point (/ (- (:width frame-size) (:width overlay-size)) 2)
                   (/ (- (:height frame-size) (:height overlay-size)) 2))

        :top-left
        (gpt/point 0 0)

        :top-right
        (gpt/point (- (:width frame-size) (:width overlay-size))
                   0)

        :top-center
        (gpt/point (/ (- (:width frame-size) (:width overlay-size)) 2)
                   0)

        :bottom-left
        (gpt/point 0
                   (- (:height frame-size) (:height overlay-size)))

        :bottom-right
        (gpt/point (- (:width frame-size) (:width overlay-size))
                   (- (:height frame-size) (:height overlay-size)))

        :bottom-center
        (gpt/point (/ (- (:width frame-size) (:width overlay-size)) 2)
                   (- (:height frame-size) (:height overlay-size)))))))

