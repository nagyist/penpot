;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) UXBOX Labs SL

(ns app.main.ui.workspace.sidebar.options.menus.booleans
  (:require
   [app.main.data.workspace :as dw]
   [app.main.refs :as refs]
   [app.main.store :as st]
   [app.main.ui.icons :as i]
   [app.util.dom :as dom]
   [app.util.i18n :as i18n :refer [tr]]
   [rumext.alpha :as mf]))

(mf/defc booleans-options
  []
  (let [selected (mf/deref refs/selected-objects)
        disabled (or (empty? selected)
                     (and (<= (count selected) 1)
                          (not (contains? #{:group :bool} (:type (first selected))))))

        head (first selected)
        is-group? (and (some? head) (= :group (:type head)))
        is-bool?  (and (some? head) (= :bool (:type head)))
        head-bool-type (and (some? head) (:bool-type head))

        set-bool
        (fn [bool-type]
          #(cond
             (> (count selected) 1)
             (st/emit! (dw/create-bool bool-type))

             (and (= (count selected) 1) is-group?)
             (st/emit! (dw/group-to-bool (:id head) bool-type))

             (and (= (count selected) 1) is-bool?)
             (if (= head-bool-type bool-type)
               (st/emit! (dw/bool-to-group (:id head)))
               (st/emit! (dw/change-bool-type (:id head) bool-type)))))]

    [:div.align-options
     [:div.align-group
      [:div.align-button.tooltip.tooltip-bottom
       {:alt (tr "workspace.shape.menu.union")
        :class (dom/classnames :disabled disabled
                               :selected (= head-bool-type :union))
        :on-click (set-bool :union)}
       i/boolean-union]

      [:div.align-button.tooltip.tooltip-bottom
       {:alt (tr "workspace.shape.menu.difference")
        :class (dom/classnames :disabled disabled
                               :selected (= head-bool-type :difference))
        :on-click (set-bool :difference)}
       i/boolean-difference]

      [:div.align-button.tooltip.tooltip-bottom
       {:alt (tr "workspace.shape.menu.intersection")
        :class (dom/classnames :disabled disabled
                               :selected (= head-bool-type :intersection))
        :on-click (set-bool :intersection)}
       i/boolean-intersection]

      [:div.align-button.tooltip.tooltip-bottom
       {:alt (tr "workspace.shape.menu.exclude")
        :class (dom/classnames :disabled disabled
                               :selected (= head-bool-type :exclude))
        :on-click (set-bool :exclude)}
       i/boolean-exclude]]]))

