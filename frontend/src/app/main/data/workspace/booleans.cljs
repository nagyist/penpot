;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) UXBOX Labs SL

(ns app.main.data.workspace.booleans
  (:require
   [app.common.data :as d]
   [app.common.geom.shapes :as gsh]
   [app.common.pages :as cp]
   [app.common.pages.changes-builder :as cb]
   [app.common.path.shapes-to-path :as stp]
   [app.common.uuid :as uuid]
   [app.main.data.workspace.changes :as dch]
   [app.main.data.workspace.common :as dwc]
   [app.main.data.workspace.state-helpers :as wsh]
   [beicon.core :as rx]
   [cuerdas.core :as str]
   [potok.core :as ptk]))

(defn selected-shapes
  [state]
  (let [objects  (wsh/lookup-page-objects state)]
    (->> (wsh/lookup-selected state)
         (cp/clean-loops objects)
         (map #(get objects %))
         (filter #(not= :frame (:type %)))
         (map #(assoc % ::index (cp/position-on-parent (:id %) objects)))
         (sort-by ::index))))

(defn create-bool-data
  [type name shapes objects]
  (let [shapes (mapv #(stp/convert-to-path % objects) shapes)
        head (first shapes)
        head-data (select-keys head stp/style-properties)]
    (-> {:id (uuid/next)
         :type :bool
         :bool-type type
         :frame-id (:frame-id head)
         :parent-id (:parent-id head)
         :name name
         ::index (::index head)
         :shapes []}
        (merge head-data)
        (gsh/update-bool-selrect shapes objects))))

(defn create-bool
  [bool-type]
  (ptk/reify ::create-bool-union
    ptk/WatchEvent
    
    (watch [it state _]
      (let [page-id (:current-page-id state)
            objects (wsh/lookup-page-objects state)
            base-name (-> bool-type d/name str/capital (str "-1"))
            name (-> (dwc/retrieve-used-names objects)
                     (dwc/generate-unique-name base-name))
            shapes  (selected-shapes state)]

        (when-not (empty? shapes)
          (let [boolean-data (create-bool-data bool-type name shapes objects)
                shape-id (:id boolean-data)
                changes (-> (cb/empty-changes it page-id)
                            (cb/add-obj boolean-data)
                            (cb/change-parent shape-id shapes))]
            (rx/of (dch/commit-changes changes)
                   (dwc/select-shapes (d/ordered-set shape-id)))))))))
