;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) KALEIDOS INC

(ns app.main.ui.workspace.sidebar.options.menus.layout-container
  (:require-macros [app.main.style :as stl])
  (:require
   [app.common.data :as d]
   [app.common.data.macros :as dm]
   [app.common.math :as mth]
   [app.common.types.shape.layout :as ctl]
   [app.config :as cf]
   [app.main.data.workspace :as udw]
   [app.main.data.workspace.grid-layout.editor :as dwge]
   [app.main.data.workspace.shape-layout :as dwsl]
   [app.main.features :as features]
   [app.main.refs :as refs]
   [app.main.store :as st]
   [app.main.ui.components.dropdown :refer [dropdown]]
   [app.main.ui.components.numeric-input :refer [numeric-input*]]
   [app.main.ui.components.radio-buttons :refer [radio-button radio-buttons]]
   [app.main.ui.components.select :refer [select]]
   [app.main.ui.components.title-bar :refer [title-bar]]
   [app.main.ui.formats :as fmt]
   [app.main.ui.hooks :as h]
   [app.main.ui.icons :as i]
   [app.util.dom :as dom]
   [app.util.i18n :as i18n :refer [tr]]
   [app.util.keyboard :as kbd]
   [cuerdas.core :as str]
   [rumext.v2 :as mf]))

(defn dir-icons-refactor
  [val]
  (case val
    :row            i/grid-row-refactor
    :row-reverse    i/row-reverse-refactor
    :column         i/column-refactor
    :column-reverse i/column-reverse-refactor))

;; FLEX COMPONENTS

(def layout-container-flex-attrs
  [:layout                 ;; :flex, :grid in the future
   :layout-flex-dir        ;; :row, :row-reverse, :column, :column-reverse
   :layout-gap-type        ;; :simple, :multiple
   :layout-gap             ;; {:row-gap number , :column-gap number}

   :layout-align-items     ;; :start :end :center :stretch
   :layout-justify-content ;; :start :center :end :space-between :space-around :space-evenly
   :layout-align-content   ;; :start :center :end :space-between :space-around :space-evenly :stretch (by default)
   :layout-wrap-type       ;; :wrap, :nowrap
   :layout-padding-type    ;; :simple, :multiple
   :layout-padding         ;; {:p1 num :p2 num :p3 num :p4 num} number could be negative

   :layout-grid-dir ;; :row :column
   :layout-justify-items
   :layout-grid-columns
   :layout-grid-rows])

(defn get-layout-flex-icon
  [type val ^boolean column?]
  (case type
    :align-items
    (if column?
      (case val
        :start    i/align-items-column-start-refactor
        :end      i/align-items-column-end-refactor
        :center   i/align-items-column-center-refactor
        :stretch  i/align-items-column-strech
        :baseline i/align-items-column-baseline)
      (case val ;; TODO Check strech and baseline icons
        :start    i/align-items-row-start-refactor
        :end      i/align-items-row-end-refactor
        :center   i/align-items-row-center-refactor
        :stretch  i/align-items-row-strech
        :baseline i/align-items-row-baseline))

    :justify-content
    (if column?
      (case val
        :start         i/justify-content-column-start-refactor
        :end           i/justify-content-column-end-refactor
        :center        i/justify-content-column-center-refactor
        :space-around  i/justify-content-column-around-refactor
        :space-evenly  i/justify-content-column-evenly-refactor
        :space-between i/justify-content-column-between-refactor)
      (case val
        :start         i/justify-content-row-start-refactor
        :end           i/justify-content-row-end-refactor
        :center        i/justify-content-row-center-refactor
        :space-around  i/justify-content-row-around-refactor
        :space-evenly  i/justify-content-row-evenly-refactor
        :space-between i/justify-content-row-between-refactor))

    :align-content
    (if column?
      (case val
        :start         i/align-content-column-start-refactor
        :end           i/align-content-column-end-refactor
        :center        i/align-content-column-center-refactor
        :space-around  i/align-content-column-around-refactor
        :space-evenly  i/align-content-column-evenly-refactor
        :space-between i/align-content-column-between-refactor
        :stretch nil)

      (case val
        :start         i/align-content-row-start-refactor
        :end           i/align-content-row-end-refactor
        :center        i/align-content-row-center-refactor
        :space-around  i/align-content-row-around-refactor
        :space-evenly  i/align-content-row-evenly-refactor
        :space-between i/align-content-row-between-refactor
        :stretch nil))

    :align-self
    (if column?
      (case val
        :auto     i/remove-refactor
        :start    i/align-self-row-left-refactor
        :end      i/align-self-row-right-refactor
        :center   i/align-self-row-center-refactor
        :stretch  i/align-self-row-strech
        :baseline i/align-self-row-baseline)
      (case val
        :auto     i/remove-refactor
        :start    i/align-self-column-top-refactor
        :end      i/align-self-column-bottom-refactor
        :center   i/align-self-column-center-refactor
        :stretch  i/align-self-column-strech
        :baseline i/align-self-column-baseline))))

(defn get-layout-grid-icon-refactor
  [type val ^boolean column?]
  (case type
    :align-items
    (if column?
      (case val
        :auto     i/remove-refactor
        :start    i/align-self-row-left-refactor
        :end      i/align-self-row-right-refactor
        :center   i/align-self-row-center-refactor
        :stretch  i/align-self-row-strech
        :baseline i/align-self-row-baseline)
      (case val
        :auto     i/remove-refactor
        :start    i/align-self-column-top-refactor
        :end      i/align-self-column-bottom-refactor
        :center   i/align-self-column-center-refactor
        :stretch  i/align-self-column-strech
        :baseline i/align-self-column-baseline))

    :justify-items
    (if (not column?)
      (case val
        :start         i/align-content-column-start-refactor
        :center        i/align-content-column-center-refactor
        :end           i/align-content-column-end-refactor
        :space-around  i/align-content-column-around-refactor
        :space-between i/align-content-column-between-refactor
        :stretch       i/align-content-column-stretch-refactor)
      (case val
        :start         i/align-content-row-start-refactor
        :center        i/align-content-row-center-refactor
        :end           i/align-content-row-end-refactor
        :space-around  i/align-content-row-around-refactor
        :space-between i/align-content-row-between-refactor
        :stretch       i/align-content-row-stretch-refactor))))

(mf/defc direction-row-flex
  {::mf/props :obj}
  [{:keys [saved-dir on-change]}]
  [:& radio-buttons {:selected (d/name saved-dir)
                     :on-change on-change
                     :name "flex-direction"}
   [:& radio-button {:value "row"
                     :id "flex-direction-row"
                     :title "Row"
                     :icon (dir-icons-refactor :row)}]
   [:& radio-button {:value "row-reverse"
                     :id "flex-direction-row-reverse"
                     :title "Row reverse"
                     :icon (dir-icons-refactor :row-reverse)}]
   [:& radio-button {:value "column"
                     :id "flex-direction-column"
                     :title "Column"
                     :icon (dir-icons-refactor :column)}]
   [:& radio-button {:value "column-reverse"
                     :id "flex-direction-column-reverse"
                     :title "Column reverse"
                     :icon (dir-icons-refactor :column-reverse)}]])

(mf/defc wrap-row
  {::mf/props :obj}
  [{:keys [wrap-type on-click]}]
  [:button {:class (stl/css-case :wrap-button true
                                 :selected (= wrap-type :wrap))
            :title (if (= :wrap wrap-type)
                     "No wrap"
                     "Wrap")
            :on-click on-click}
   i/wrap-refactor])

(mf/defc align-row
  {::mf/props :obj}
  [{:keys [is-column align-items on-change]}]
  [:& radio-buttons {:selected (d/name align-items)
                     :on-change on-change
                     :name "flex-align-items"}
   [:& radio-button {:value "start"
                     :icon  (get-layout-flex-icon :align-items :start is-column)
                     :title "Align items start"
                     :id     "align-items-start"}]
   [:& radio-button {:value "center"
                     :icon  (get-layout-flex-icon :align-items :center is-column)
                     :title "Align items center"
                     :id    "align-items-center"}]
   [:& radio-button {:value "end"
                     :icon  (get-layout-flex-icon :align-items :end is-column)
                     :title "Align items end"
                     :id    "align-items-end"}]])

(mf/defc align-content-row
  {::mf/props :obj}
  [{:keys [is-column align-content on-change]}]
  [:& radio-buttons {:selected (d/name align-content)
                     :on-change on-change
                     :name "flex-align-content"}
   [:& radio-button {:value      "start"
                     :icon       (get-layout-flex-icon :align-content :start is-column)
                     :title      "Align content start"
                     :id         "align-content-start"}]
   [:& radio-button {:value      "center"
                     :icon       (get-layout-flex-icon :align-content :center is-column)
                     :title      "Align content center"
                     :id         "align-content-center"}]
   [:& radio-button {:value      "end"
                     :icon       (get-layout-flex-icon :align-content :end is-column)
                     :title      "Align content end"
                     :id         "align-content-end"}]
   [:& radio-button {:value      "space-between"
                     :icon       (get-layout-flex-icon :align-content :space-between is-column)
                     :title      "Align content space-between"
                     :id         "align-content-space-between"}]
   [:& radio-button {:value      "space-around"
                     :icon       (get-layout-flex-icon :align-content :space-around is-column)
                     :title      "Align content space-around"
                     :id         "align-content-space-around"}]
   [:& radio-button {:value      "space-evenly"
                     :icon       (get-layout-flex-icon :align-content :space-evenly is-column)
                     :title      "Align content space-evenly"
                     :id         "align-content-space-evenly"}]])

(mf/defc justify-content-row
  {::mf/props :obj}
  [{:keys [is-column justify-content on-change]}]
  [:& radio-buttons {:selected (d/name justify-content)
                     :on-change on-change
                     :name "flex-justify"}
   [:& radio-button {:value "start"
                     :icon  (get-layout-flex-icon :justify-content :start is-column)
                     :title "Justify content start"
                     :id    "justify-content-start"}]
   [:& radio-button {:value "center"
                     :icon  (get-layout-flex-icon :justify-content :center is-column)
                     :title "Justify content center"
                     :id    "justify-content-center"}]
   [:& radio-button {:value "end"
                     :icon  (get-layout-flex-icon :justify-content :end is-column)
                     :title "Justify content end"
                     :id    "justify-content-end"}]
   [:& radio-button {:value "space-between"
                     :icon  (get-layout-flex-icon :justify-content :space-between is-column)
                     :title "Justify content space-between"
                     :id    "justify-content-space-between"}]
   [:& radio-button {:value "space-around"
                     :icon  (get-layout-flex-icon :justify-content :space-around is-column)
                     :title "Justify content space-around"
                     :id    "justify-content-space-around"}]
   [:& radio-button {:value "space-evenly"
                     :icon  (get-layout-flex-icon :justify-content :space-evenly is-column)
                     :title "Justify content space-evenly"
                     :id    "justify-content-space-evenly"}]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PADDING
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- select-padding
  ([p]
   (select-padding (= p :p1) (= p :p2) (= p :p3) (= p :p4)))
  ([p1? p2? p3? p4?]
   (st/emit! (udw/set-paddings-selected {:p1 p1? :p2 p2? :p3 p3? :p4 p4?}))))

(defn- on-padding-blur
  [_event]
  (select-padding false false false false))

(mf/defc simple-padding-selection
  {::mf/props :obj}
  [{:keys [padding on-change]}]
  (let [p1 (:p1 padding)
        p2 (:p2 padding)
        p3 (:p3 padding)
        p4 (:p4 padding)

        p1 (if (and (not (= :multiple padding))
                    (= p1 p3))
             p1
             "--")

        p2 (if (and (not (= :multiple padding))
                    (= p2 p4))
             p2
             "--")

        on-change'
        (mf/use-fn
         (mf/deps on-change)
         (fn [value event]
           (let [attr (-> (dom/get-current-target event)
                          (dom/get-data "attr")
                          (keyword))]
             (on-change :simple attr value event))))

        on-focus
        (mf/use-fn
         (fn [event]
           (let [attr (-> (dom/get-current-target event)
                          (dom/get-data "attr")
                          (keyword))]

             (case attr
               :p1 (select-padding true false true false)
               :p2 (select-padding false true false true))

             (dom/select-target event))))]

    [:div {:class (stl/css :paddings-simple)}
     [:div {:class (stl/css :padding-simple)
            :title "Vertical padding"}
      [:span {:class (stl/css :icon)}
       i/padding-top-bottom-refactor]
      [:> numeric-input*
       {:class (stl/css :numeric-input)
        :placeholder "--"
        :data-attr "p1"
        :on-change on-change'
        :on-focus on-focus
        :nillable true
        :min 0
        :value p1}]]
     [:div {:class (stl/css :padding-simple)
            :title "Horizontal padding"}

      [:span {:class (stl/css :icon)}
       i/padding-left-right-refactor]
      [:> numeric-input*
       {:className (stl/css :numeric-input)
        :placeholder "--"
        :data-attr "p2"
        :on-change on-change'
        :on-focus on-focus
        :on-blur on-padding-blur
        :nillable true
        :min 0
        :value p2}]]]))

(mf/defc multiple-padding-selection
  {::mf/props :obj}
  [{:keys [padding on-change]}]
  (let [p1 (:p1 padding)
        p2 (:p2 padding)
        p3 (:p3 padding)
        p4 (:p4 padding)

        on-change'
        (mf/use-fn
         (mf/deps on-change)
         (fn [value event]
           (let [attr (-> (dom/get-current-target event)
                          (dom/get-data "attr")
                          (keyword))]
             (on-change :multiple attr value event))))

        on-focus
        (mf/use-fn
         (fn [event]
           (let [attr (-> (dom/get-current-target event)
                          (dom/get-data "attr")
                          (keyword))]

             (select-padding attr)
             (dom/select-target event))))]

    [:div {:class (stl/css :paddings-multiple)}
     [:div {:class (stl/css :padding-multiple)
            :title "Top padding"}
      [:span {:class (stl/css :icon)}
       i/padding-top-refactor]
      [:> numeric-input*
       {:class (stl/css :numeric-input)
        :placeholder "--"
        :data-attr "p1"
        :on-change on-change'
        :on-focus on-focus
        :on-blur on-padding-blur
        :nillable true
        :min 0
        :value p1}]]

     [:div {:class (stl/css :padding-multiple)
            :title "Right padding"}
      [:span {:class (stl/css :icon)}
       i/padding-right-refactor]
      [:> numeric-input*
       {:class (stl/css :numeric-input)
        :placeholder "--"
        :data-attr "p2"
        :on-change on-change'
        :on-focus on-focus
        :on-blur on-padding-blur
        :nillable true
        :min 0
        :value p2}]]

     [:div {:class (stl/css :padding-multiple)
            :title "Bottom padding"}
      [:span {:class (stl/css :icon)}
       i/padding-bottom-refactor]
      [:> numeric-input*
       {:class (stl/css :numeric-input)
        :placeholder "--"
        :data-attr "p3"
        :on-change on-change'
        :on-focus on-focus
        :on-blur on-padding-blur
        :nillable true
        :min 0
        :value p3}]]

     [:div {:class (stl/css :padding-multiple)
            :title "Left padding"}
      [:span {:class (stl/css :icon)}
       i/padding-left-refactor]
      [:> numeric-input*
       {:class (stl/css :numeric-input)
        :placeholder "--"
        :data-attr "p4"
        :on-change on-change'
        :on-focus on-focus
        :on-blur on-padding-blur
        :nillable true
        :min 0
        :value p4}]]]))

(mf/defc padding-section
  {::mf/props :obj}
  [{:keys [type on-type-change on-change] :as props}]
  (let [on-type-change'
        (mf/use-fn
         (mf/deps on-type-change)
         (fn [event]
           (let [type (-> (dom/get-current-target event)
                          (dom/get-data "type"))
                 type (if (= type "multiple") :simple :multiple)]
             (on-type-change type))))

        props (mf/spread-obj props {:on-change on-change})]

    (mf/with-effect []
      ;; on destroy component
      (fn []
        (on-padding-blur nil)))

    [:div {:class (stl/css :padding-group)}
     [:div {:class (stl/css :padding-inputs)}
      (cond
        (= type :simple)
        [:> simple-padding-selection props]

        (= type :multiple)
        [:> multiple-padding-selection props])]

     [:button {:class (stl/css-case
                       :padding-toggle true
                       :selected (= type :multiple))
               :data-type (name type)
               :on-click on-type-change'}
      i/padding-extended-refactor]]))

(mf/defc gap-section
  {::mf/props :obj}
  [{:keys [is-column wrap-type on-change gap-value]}]
  (let [select-gap
        (fn [gap]
          (st/emit! (udw/set-gap-selected gap)))]

    (mf/use-effect
     (fn []
       (fn []
         ;;on destroy component
         (select-gap nil))))

    [:div {:class (stl/css :gap-group)}
     [:div {:class (stl/css-case :row-gap true
                                 :disabled (and (= :nowrap wrap-type) (not is-column)))
            :title "Row gap"}
      [:span {:class (stl/css :icon)} i/gap-vertical-refactor]
      [:> numeric-input* {:class (stl/css :numeric-input true)
                          :no-validate true
                          :placeholder "--"
                          :on-focus (fn [event]
                                      (select-gap :row-gap)
                                      (dom/select-target event))
                          :on-change (partial on-change (= :nowrap wrap-type) :row-gap)
                          :on-blur (fn [_]
                                     (select-gap nil))
                          :nillable true
                          :min 0
                          :value (:row-gap gap-value)
                          :disabled (and (= :nowrap wrap-type) (not is-column))}]]

     [:div {:class (stl/css-case :column-gap true
                                 :disabled (and (= :nowrap wrap-type) is-column))
            :title "Column gap"}
      [:span {:class (stl/css :icon)}
       i/gap-horizontal-refactor]
      [:> numeric-input* {:className (stl/css :numeric-input)
                          :no-validate true
                          :placeholder "--"
                          :on-focus (fn [event]
                                      (select-gap :column-gap)
                                      (dom/select-target event))
                          :on-change (partial on-change (= :nowrap wrap-type) :column-gap)
                          :on-blur (fn [_]
                                     (select-gap nil)
                                     )
                          :nillable true
                          :min 0
                          :value (:column-gap gap-value)
                          :disabled (and (= :nowrap wrap-type) is-column)}]]]))

;; GRID COMPONENTS

(defn get-layout-grid-icon
  [type val ^boolean column?]
  (case type
    :justify-items
    (if column?
      (case val
        :stretch       i/align-items-row-strech
        :start         i/grid-justify-content-column-start
        :end           i/grid-justify-content-column-end
        :center        i/grid-justify-content-column-center
        :space-around  i/grid-justify-content-column-around
        :space-between i/grid-justify-content-column-between
        :space-evenly  i/grid-justify-content-column-between)

      (case val
        :stretch       i/align-items-column-strech
        :start         i/grid-justify-content-row-start
        :end           i/grid-justify-content-row-end
        :center        i/grid-justify-content-row-center
        :space-around  i/grid-justify-content-row-around
        :space-between i/grid-justify-content-row-between
        :space-evenly  i/grid-justify-content-row-between))))

(mf/defc direction-row-grid
  {::mf/props :obj}
  [{:keys [saved-dir on-change] :as props}]
  [:& radio-buttons {:selected (d/name saved-dir)
                     :on-change on-change
                     :name "grid-direction"}
   [:& radio-button {:value "row"
                     :id "grid-direction-row"
                     :title "Row"
                     :icon (dir-icons-refactor :row)}]
   [:& radio-button {:value "column"
                     :id "grid-direction-column"
                     :title "Column"
                     :icon (dir-icons-refactor :column)}]])

(mf/defc grid-edit-mode
  {::mf/props :obj}
  [{:keys [id]}]
  (let [edition (mf/deref refs/selected-edition)
        active? (= id edition)

        toggle-edit-mode
        (mf/use-fn
         (mf/deps id edition)
         (fn []
           (if-not active?
             (st/emit! (udw/start-edition-mode id))
             (st/emit! :interrupt))))]
    [:button
     {:class (stl/css :edit-mode-btn)
      :alt  "Grid edit mode"
      :on-click toggle-edit-mode}
     (tr "workspace.layout_grid.editor.options.edit-grid")]))

(mf/defc align-grid-row
  {::mf/props :obj}
  [{:keys [is-column align-items set-align]}]
  (let [type (if is-column :column :row)]
    [:& radio-buttons {:selected (d/name align-items)
                       :on-change #(set-align % type)
                       :name (dm/str "flex-align-items-" (d/name type))}
     [:& radio-button {:value "start"
                       :icon  (get-layout-grid-icon-refactor :align-items :start is-column)
                       :title "Align items start"
                       :id     (dm/str "align-items-start-" (d/name type))}]
     [:& radio-button {:value "center"
                       :icon  (get-layout-grid-icon-refactor :align-items :center is-column)
                       :title "Align items center"
                       :id    (dm/str "align-items-center-" (d/name type))}]
     [:& radio-button {:value "end"
                       :icon  (get-layout-grid-icon-refactor :align-items :end is-column)
                       :title "Align items end"
                       :id    (dm/str "align-items-end-" (d/name type))}]]))

(mf/defc justify-grid-row
  {::mf/props :obj}
  [{:keys [is-column justify-items set-justify]}]
  (let [type (if is-column :column :row)]

    [:& radio-buttons {:selected (d/name justify-items)
                       :on-change #(set-justify % type)
                       :name (dm/str "grid-justify-items-" (d/name type))}
     (for [justify [:start :center :end :space-around :space-between :stretch]]
       [:& radio-button {:key   (dm/str "justify-item-" (d/name justify))
                         :value (d/name justify)
                         :icon  (get-layout-grid-icon-refactor :justify-items justify is-column)
                         :title (dm/str "Justify items " (d/name justify))
                         :id    (dm/str "justify-items-" (d/name justify) "-" (d/name type))}])]))

(defn- manage-values
  [{:keys [type value]}]
  (case type
    :auto "auto"
    :percent (fmt/format-percent value)
    :flex    (fmt/format-frs value)
    :fixed   (fmt/format-pixels value)
    value))

(mf/defc grid-track-info
  {::mf/props :obj}
  [{:keys [is-column
           type
           index
           column
           set-column-value
           set-column-type
           remove-element
           reorder-track
           hover-track
           on-select-track]}]

  (let [drop-track
        (mf/use-fn
         (mf/deps type reorder-track index)
         (fn [drop-position data event]
           (reorder-track type (:index data) (if (= :top drop-position) (dec index) index) (not (kbd/mod? event)))))

        pointer-enter
        (mf/use-fn
         (mf/deps type hover-track index)
         (fn []
           (hover-track type index true)))

        pointer-leave
        (mf/use-fn
         (mf/deps type hover-track index)
         (fn []
           (hover-track type index false)))

        handle-select-track
        (mf/use-fn
         (mf/deps on-select-track type index)
         (fn []
           (when on-select-track
             (on-select-track type index))))

        [dprops dref]
        (h/use-sortable
         :data-type "penpot/grid-track"
         :on-drop drop-track
         :data {:is-column is-column
                :index index
                :column column}
         :draggable? true)]

    [:div {:class (stl/css-case :track-info true
                                :dnd-over-top (or (= (:over dprops) :top)
                                                  (= (:over dprops) :center))
                                :dnd-over-bot (= (:over dprops) :bot))
           :ref dref
           :on-pointer-enter pointer-enter
           :on-pointer-leave pointer-leave}

     [:div {:class (stl/css :track-info-container)}
      [:div {:class (stl/css :track-info-dir-icon)
             :on-click handle-select-track}
       (if is-column i/flex-vertical-refactor i/flex-horizontal-refactor)]

      [:div {:class (stl/css :track-info-value)}
       [:> numeric-input* {:no-validate true
                           :value (:value column)
                           :on-change #(set-column-value type index %)
                           :placeholder "--"
                           :min 0
                           :disabled (= :auto (:type column))}]]

      [:div {:class (stl/css :track-info-unit)}
       [:& select {:class (stl/css :track-info-unit-selector)
                   :default-value (:type column)
                   :options [{:value :flex :label "FR"}
                             {:value :auto :label "AUTO"}
                             {:value :fixed :label "PX"}
                             {:value :percent :label "%"}]
                   :on-change #(set-column-type type index %)}]]]

     [:button {:class (stl/css :remove-track-btn)
               :on-click #(remove-element type index)}
      i/remove-refactor]]))

(mf/defc grid-columns-row
  {::mf/props :obj}
  [{:keys [is-column expanded? column-values toggle add-new-element set-column-value set-column-type
           remove-element reorder-track hover-track on-select-track]}]
  (let [column-num (count column-values)
        direction (if (> column-num 1)
                    (if ^boolean is-column "Columns " "Rows ")
                    (if ^boolean is-column "Column " "Row "))

        track-name (dm/str direction  (if (= column-num 0) " - empty" column-num))
        track-detail (str/join ", " (map manage-values column-values))

        type (if is-column :column :row)

        add-track
        #(do
           (when-not expanded? (toggle))
           (add-new-element type ctl/default-track-value))]

    [:div {:class (stl/css :grid-tracks)}
     [:div {:class (stl/css :grid-track-header)}
      [:button {:class (stl/css :expand-icon) :on-click toggle} i/menu-refactor]
      [:div {:class (stl/css :track-title) :on-click toggle}
       [:div {:class (stl/css :track-name) :title track-name} track-name]
       [:div {:class (stl/css :track-detail) :title track-detail} track-detail]]
      [:button {:class (stl/css :add-column) :on-click add-track} i/add-refactor]]

     (when expanded?
       [:& h/sortable-container {}
        [:div {:class (stl/css :grid-tracks-info-container)}
         (for [[index column] (d/enumerate column-values)]
           [:& grid-track-info {:key (dm/str index "-" (name type))
                                :type type
                                :is-column is-column
                                :index index
                                :column column
                                :set-column-value set-column-value
                                :set-column-type set-column-type
                                :remove-element remove-element
                                :reorder-track reorder-track
                                :hover-track hover-track
                                :on-select-track on-select-track}])]])]))

;; LAYOUT COMPONENT

(mf/defc layout-container-menu
  {::mf/memo #{:ids :values :multiple}
   ::mf/props :obj}
  [{:keys [ids values multiple]}]
  (let [;; Display
        layout-type         (:layout values)
        has-layout?         (some? layout-type)

        show-layout-dropdown* (mf/use-state false)
        show-layout-dropdown? @show-layout-dropdown*

        state*              (mf/use-state (if layout-type
                                            true
                                            false))

        open? (deref state*)
        toggle-content (mf/use-fn #(swap! state* not))

        on-add-layout
        (mf/use-fn
         (fn [type]
           (st/emit! (dwsl/create-layout type))
           (reset! state* true)))

        on-set-layout
        (mf/use-fn
         (fn [event]
           (let [value (-> (dom/get-current-target event)
                           (dom/get-data "value")
                           (keyword))]
             (on-add-layout value))))

        on-remove-layout
        (fn [_]
          (st/emit! (dwsl/remove-layout ids))
          (reset! state* false))

        set-flex
        (mf/use-fn
         (mf/deps on-add-layout)
         (fn []
           (on-add-layout :flex)))

        set-grid
        (mf/use-fn
         (mf/deps on-add-layout)
         (fn []
           (on-add-layout :grid)))

        saved-dir (:layout-flex-dir values)

        is-column   (or (= :column saved-dir) (= :column-reverse saved-dir))

        set-direction-refactor
        (mf/use-fn
         (mf/deps layout-type ids)
         (fn [dir]
           (let [dir (keyword dir)]
             (if (= :flex layout-type)
               (st/emit! (dwsl/update-layout ids {:layout-flex-dir dir}))
               (st/emit! (dwsl/update-layout ids {:layout-grid-dir dir}))))))

        ;; Wrap type

        wrap-type (:layout-wrap-type values)

        toggle-wrap
        (mf/use-fn
         (mf/deps wrap-type ids)
         (fn []
           (let [type (if (= wrap-type :wrap)
                        :nowrap
                        :wrap)]
             (st/emit! (dwsl/update-layout ids {:layout-wrap-type type})))))


        ;; Align items

        align-items         (:layout-align-items values)

        set-align-items
        (mf/use-fn
         (mf/deps ids)
         (fn [value]
           (st/emit! (dwsl/update-layout ids {:layout-align-items (keyword value)}))))

        ;; Justify content

        justify-content     (:layout-justify-content values)

        set-justify-content
        (mf/use-fn
         (mf/deps ids)
         (fn [value]
           (st/emit! (dwsl/update-layout ids {:layout-justify-content (keyword value)}))))

        ;; Align content

        align-content         (:layout-align-content values)

        set-align-content
        (mf/use-fn
         (mf/deps ids)
         (fn [value align-content]
           (if (= align-content value)
             (st/emit! (dwsl/update-layout ids {:layout-align-content :stretch}))
             (st/emit! (dwsl/update-layout ids {:layout-align-content (keyword value)})))))


        ;; Gap
        set-gap
        (fn [gap-multiple? type val]
          (let [val (mth/finite val 0)]
            (if gap-multiple?
              (st/emit! (dwsl/update-layout ids {:layout-gap {:row-gap val :column-gap val}}))
              (st/emit! (dwsl/update-layout ids {:layout-gap {type val}})))))

        ;; Padding

        change-padding-type
        (fn [type]
          (st/emit! (dwsl/update-layout ids {:layout-padding-type type})))

        on-padding-change
        (mf/use-fn
         (mf/deps ids)
         (fn [type prop val]
           (let [val (mth/finite val 0)]
             (cond
               (and (= type :simple) (= prop :p1))
               (st/emit! (dwsl/update-layout ids {:layout-padding {:p1 val :p3 val}}))

               (and (= type :simple) (= prop :p2))
               (st/emit! (dwsl/update-layout ids {:layout-padding {:p2 val :p4 val}}))

               :else
               (st/emit! (dwsl/update-layout ids {:layout-padding {prop val}}))))))

        ;; Grid-direction

        saved-grid-dir (:layout-grid-dir values)

        set-direction
        (mf/use-fn
         (mf/deps layout-type ids)
         (fn [dir]
           (let [dir (keyword dir)]
             (if (= :flex layout-type)
               (st/emit! (dwsl/update-layout ids {:layout-grid-dir dir}))
               (st/emit! (dwsl/update-layout ids {:layout-grid-dir dir}))))))

        ;; Align grid
        align-items-row    (:layout-align-items values)
        align-items-column (:layout-justify-items values)

        set-align-grid
        (mf/use-fn
         (mf/deps ids)
         (fn [value type]
           (let [value (keyword value)]
             (if (= type :row)
               (st/emit! (dwsl/update-layout ids {:layout-align-items value}))
               (st/emit! (dwsl/update-layout ids {:layout-justify-items value}))))))

        ;; Justify grid
        grid-justify-content-row    (:layout-justify-content values)
        grid-justify-content-column (:layout-align-content values)

        grid-enabled?  (features/use-feature "layout/grid")

        set-justify-grid
        (mf/use-fn
         (mf/deps ids)
         (fn [value type]
           (let [value (keyword value)]
             (if (= type :row)
               (st/emit! (dwsl/update-layout ids {:layout-justify-content value}))
               (st/emit! (dwsl/update-layout ids {:layout-align-content value}))))))

        handle-show-layout-dropdown
        (mf/use-fn
         (fn []
           (swap! show-layout-dropdown* not)))

        handle-close-layout-options
        (mf/use-fn
         (fn []
           (reset! show-layout-dropdown* false)))

        handle-open-flex-help
        (mf/use-fn
         (fn []
           (st/emit! (dom/open-new-window cf/flex-help-uri))))

        handle-open-grid-help
        (mf/use-fn
         (fn []
           (st/emit! (dom/open-new-window cf/grid-help-uri))))]

    [:div {:class (stl/css :element-set)}
     [:div {:class (stl/css :element-title)}
      [:& title-bar {:collapsable  has-layout?
                     :collapsed    (not open?)
                     :on-collapsed toggle-content
                     :title        "Layout"
                     :class        (stl/css-case :title-spacing-layout (not has-layout?))}
       (if (and (not multiple) (:layout values))
         [:div {:class (stl/css :title-actions)}
          (when ^boolean grid-enabled?
            [:*
             [:button {:class (stl/css :add-layout)
                       :on-click handle-show-layout-dropdown}
              i/menu-refactor]

             [:& dropdown {:show show-layout-dropdown? :on-close handle-close-layout-options}
              [:div {:class (stl/css :layout-options)}
               [:button {:class (stl/css :layout-option) :on-click set-flex} "Flex layout"]
               [:button {:class (stl/css :layout-option) :on-click set-grid} "Grid layout"]]]])

          (when has-layout?
            [:button {:class (stl/css :remove-layout)
                      :on-click on-remove-layout}
             i/remove-refactor])]

         [:div {:class (stl/css :title-actions)}
          (if ^boolean grid-enabled?
            [:*
             [:button {:class (stl/css :add-layout)
                       :on-click handle-show-layout-dropdown}
              i/add-refactor]

             [:& dropdown {:show show-layout-dropdown? :on-close handle-close-layout-options}
              [:div {:class (stl/css :layout-options)}
               [:button {:class (stl/css :layout-option) :on-click set-flex} "Flex layout"]
               [:button {:class (stl/css :layout-option) :on-click set-grid} "Grid layout"]]]]

            [:button {:class (stl/css :add-layout)
                      :data-value "flex"
                      :on-click on-set-layout}
             i/add-refactor])
          (when has-layout?
            [:button {:class (stl/css :remove-layout)
                      :on-click on-remove-layout}
             i/remove-refactor])])]]

     (when (and open? has-layout?)
       (when (not= :multiple layout-type)
         (case layout-type
           :flex
           [:div  {:class (stl/css :flex-layout-menu)}
            [:div {:class (stl/css :first-row)}
             [:& align-row {:is-column is-column
                            :align-items align-items
                            :on-change set-align-items}]

             [:& direction-row-flex {:on-change set-direction-refactor
                                     :saved-dir saved-dir}]

             [:& wrap-row {:wrap-type wrap-type
                           :on-click toggle-wrap}]]

            [:div {:class (stl/css :second-row :help-button-wrapper)}
             [:& justify-content-row {:is-column is-column
                                      :justify-content justify-content
                                      :on-change set-justify-content}]

             [:button {:on-click handle-open-flex-help
                       :class (stl/css :help-button)} i/help-refactor]]
            (when (= :wrap wrap-type)
              [:div {:class (stl/css :third-row)}
               [:& align-content-row {:is-column is-column
                                      :align-content align-content
                                      :on-change set-align-content}]])
            [:div {:class (stl/css :forth-row)}
             [:& gap-section {:is-column is-column
                              :wrap-type wrap-type
                              :on-change set-gap
                              :gap-value (:layout-gap values)}]

             [:& padding-section {:padding (:layout-padding values)
                                  :type (:layout-padding-type values)
                                  :on-type-change change-padding-type
                                  :on-change on-padding-change}]]]

           :grid
           [:div {:class (stl/css :grid-layout-menu)}
            (when (= 1 (count ids))
              [:div {:class (stl/css :edit-grid-wrapper)}
               [:& grid-edit-mode {:id (first ids)}]
               [:button {:on-click handle-open-grid-help
                         :class (stl/css :help-button)} i/help-refactor]])
            [:div {:class (stl/css :row :first-row)}
             [:div {:class (stl/css :direction-edit)}
              [:div {:class (stl/css :direction)}
               [:& direction-row-grid {:saved-dir saved-grid-dir
                                       :on-change set-direction}]]]

             [:& align-grid-row {:is-column false
                                 :align-items align-items-row
                                 :set-align set-align-grid}]

             [:& align-grid-row {:is-column true
                                 :align-items align-items-column
                                 :set-align set-align-grid}]]

            [:div {:class (stl/css :row :grid-layout-align)}
             [:& justify-grid-row {:is-column true
                                   :justify-items grid-justify-content-column
                                   :set-justify set-justify-grid}]
             [:& justify-grid-row {:is-column false
                                   :justify-items grid-justify-content-row
                                   :set-justify set-justify-grid}]]
            [:div {:class (stl/css :row)}
             [:& gap-section {:gap-selected? gap-selected?
                              :on-change set-gap
                              :gap-value (:layout-gap values)}]]

            [:div {:class (stl/css :row :padding-section)}
             [:& padding-section {:values values
                                  :on-change-style change-padding-type
                                  :on-change on-padding-change}]]]
           nil)))]))

(mf/defc grid-layout-edition
  {::mf/memo #{:ids :values}
   ::mf/props :obj}
  [{:keys [ids values]}]
  (let [;; Gap
        saved-grid-dir (:layout-grid-dir values)

        set-direction
        (mf/use-fn
         (mf/deps ids)
         (fn [dir]
           (let [dir (keyword dir)]
             (st/emit! (dwsl/update-layout ids {:layout-grid-dir dir})))))

        on-gap-change
        (fn [gap-multiple? type val]
          (if gap-multiple?
            (st/emit! (dwsl/update-layout ids {:layout-gap {:row-gap val :column-gap val}}))
            (st/emit! (dwsl/update-layout ids {:layout-gap {type val}}))))

        ;; Padding
        change-padding-type
        (mf/use-fn
         (mf/deps ids)
         (fn [type]
           (st/emit! (dwsl/update-layout ids {:layout-padding-type type}))))

        on-padding-change
        (fn [type prop val]
          (cond
            (and (= type :simple) (= prop :p1))
            (st/emit! (dwsl/update-layout ids {:layout-padding {:p1 val :p3 val}}))

            (and (= type :simple) (= prop :p2))
            (st/emit! (dwsl/update-layout ids {:layout-padding {:p2 val :p4 val}}))

            :else
            (st/emit! (dwsl/update-layout ids {:layout-padding {prop val}}))))

        ;; Align grid
        align-items-row    (:layout-align-items values)
        align-items-column (:layout-justify-items values)

        set-align-grid
        (mf/use-fn
         (mf/deps ids)
         (fn [value type]
           (let [value (keyword value)]
             (if (= type :row)
               (st/emit! (dwsl/update-layout ids {:layout-align-items value}))
               (st/emit! (dwsl/update-layout ids {:layout-justify-items value}))))))


        ;; Justify grid
        grid-justify-content-row    (:layout-justify-content values)
        grid-justify-content-column (:layout-align-content values)

        set-justify-grid
        (mf/use-fn
         (mf/deps ids)
         (fn [value type]
           (let [value (keyword value)]
             (if (= type :row)
               (st/emit! (dwsl/update-layout ids {:layout-justify-content value}))
               (st/emit! (dwsl/update-layout ids {:layout-align-content value}))))))

        columns-open?    (mf/use-state false)
        rows-open?       (mf/use-state false)

        column-values    (:layout-grid-columns values)
        rows-values      (:layout-grid-rows values)

        toggle-columns-open
        (mf/use-fn #(swap! columns-open? not))

        toggle-rows-open
        (mf/use-fn #(swap! rows-open? not))

        add-new-element
        (mf/use-fn
         (mf/deps ids)
         (fn [type value]
           (st/emit! (dwsl/add-layout-track ids type value))))

        remove-element
        (mf/use-fn
         (mf/deps ids)
         (fn [type index]
           (st/emit! (dwsl/remove-layout-track ids type index))))

        reorder-track
        (mf/use-fn
         (mf/deps ids)
         (fn [type from-index to-index move-content?]
           (st/emit! (dwsl/reorder-layout-track ids type from-index to-index move-content?))))

        hover-track
        (mf/use-fn
         (mf/deps ids)
         (fn [type index hover?]
           (st/emit! (dwsl/hover-layout-track ids type index hover?))))

        handle-select-track
        (mf/use-fn
         (mf/deps ids)
         (fn [type index]
           (st/emit! (dwge/select-track-cells (first ids) type index))))

        set-column-value
        (mf/use-fn
         (mf/deps ids)
         (fn [type index value]
           (st/emit! (dwsl/change-layout-track ids type index {:value value}))))

        set-column-type
        (mf/use-fn
         (mf/deps ids)
         (fn [type index track-type]
           (let [value (case track-type
                         :auto nil
                         :flex 1
                         :percent 20
                         :fixed 100)]
             (st/emit! (dwsl/change-layout-track ids type index {:value value
                                                                 :type track-type})))))
        handle-open-grid-help
        (mf/use-fn
         (fn []
           (st/emit! (dom/open-new-window cf/grid-help-uri))))

        handle-locate-grid
        (mf/use-fn
         (fn []
           (st/emit! (dwge/locate-board (first ids)))))]

    [:div {:class (stl/css :grid-layout-menu)}
     [:div {:class (stl/css :row)}
      [:div {:class (stl/css :grid-layout-menu-title)} "GRID LAYOUT"]
      [:button {:on-click handle-open-grid-help
                :class (stl/css :help-button)} i/help-refactor]
      [:button {:class (stl/css :exit-btn)
                :on-click #(st/emit! udw/clear-edition-mode)}
       (tr "workspace.layout_grid.editor.options.exit")]]

     [:div {:class (stl/css :row :first-row)}
      [:div {:class (stl/css :direction-edit)}
       [:div {:class (stl/css :direction)}
        [:& direction-row-grid {:saved-dir saved-grid-dir
                                :on-change set-direction}]]]

      [:& align-grid-row {:is-column false
                          :align-items align-items-row
                          :set-align set-align-grid}]

      [:& align-grid-row {:is-column true
                          :align-items align-items-column
                          :set-align set-align-grid}]]

     [:div {:class (stl/css :row :grid-layout-align)}
      [:& justify-grid-row {:is-column true
                            :justify-items grid-justify-content-column
                            :set-justify set-justify-grid}]
      [:& justify-grid-row {:is-column false
                            :justify-items grid-justify-content-row
                            :set-justify set-justify-grid}]

      [:button {:on-click handle-locate-grid
                :class (stl/css :locate-button)}
       i/locate-refactor]]

     [:div {:class (stl/css :row)}
      [:& gap-section {:gap-selected? gap-selected?
                       :on-change set-gap
                       :gap-value (:layout-gap values)}]]

     [:div {:class (stl/css :row :padding-section)}
      [:& padding-section {:values values
                           :on-change-style change-padding-type
                           :on-change on-padding-change}]]

     [:div {:class (stl/css :row :grid-tracks-row)}
      [:& grid-columns-row {:is-column true
                            :expanded? @columns-open?
                            :toggle toggle-columns-open
                            :column-values column-values
                            :add-new-element add-new-element
                            :set-column-value set-column-value
                            :set-column-type set-column-type
                            :remove-element remove-element
                            :reorder-track reorder-track
                            :hover-track hover-track
                            :on-select-track handle-select-track}]

      [:& grid-columns-row {:is-column false
                            :expanded? @rows-open?
                            :toggle toggle-rows-open
                            :column-values rows-values
                            :add-new-element add-new-element
                            :set-column-value set-column-value
                            :set-column-type set-column-type
                            :remove-element remove-element
                            :reorder-track reorder-track
                            :hover-track hover-track
                            :on-select-track handle-select-track}]]]))
