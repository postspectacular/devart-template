(ns exhibit.panelgen2
  (:require
   [exhibit.utils :refer :all]
   [exhibit.canopy :refer :all]
   [exhibit.plinths :refer :all]
   [thi.ng.geom.core :as g]
   [thi.ng.geom.core.vector :as v :refer [vec3 V3Y V3Z]]
   [thi.ng.geom.core.matrix :as mat :refer [M44]]
   [thi.ng.geom.aabb :as a]
   [thi.ng.geom.plane :as pl]
   [thi.ng.geom.quad :as q]
   [thi.ng.geom.gmesh :as gm]
   [thi.ng.geom.mesh.csg :as csg]
   [thi.ng.geom.mesh.io :as mio]
   [thi.ng.morphogen.core :as mg]
   [thi.ng.common.data.core :as d]
   [thi.ng.common.math.core :as m :refer [*eps* HALF_PI PI TWO_PI]]
   [thi.ng.macromath.core :as mm]
   [clojure.java.io :as io])
  (:import
   [thi.ng.geom.core.vector Vec3]))

(defn make-pedals
  [ai al]
  (mg/subdiv-inset
   :dir :z :inset ai
   :out {4 (mg/extrude
            :dir :f :len al
            :out [(mg/subdiv
                   :slices 2
                   :out [{}
                         {:op :scale-side
                          :args {:side :f :scale 0.25}
                          :out [{}]}])])}))

(defn make-tree
  [o1 o2 i1 i2 i3 nrows el leaf]
  (let [inner (fn [id i l2]
                (let [l1 (if (pos? i) (mg/subdiv-inset :dir :z :inset i :out {4 nil}) {})]
                  (mg/subdiv id 3 :out [l2 l1 l2])))
        s41 (mg/subdiv-inset
             :dir :z :inset i2
             :out {4 (inner :cols i3 (inner :rows (/ i3 2) nil))})
        s42 (mg/subdiv-inset
             :dir :z :inset i2
             :out {4 (inner :rows i3 (inner :cols (/ i3 2) nil))})
        ext (fn [dir1 n dir2 elen]
              (mg/extrude
               :dir :f :len 0.0075
               :out [(mg/subdiv
                      :slices 3
                      :out [{} {}
                            (mg/subdiv
                             dir1 n
                             :out {(int (/ n 2))
                                   (mg/extrude
                                    :dir dir2 :len elen
                                    :out [(or leaf {})])})])]))
        s3 (mg/subdiv-inset
            :dir :z :inset i1
            :out [(assoc-in s41 [:out 1] (ext :cols 3 :s el))
                  (assoc-in s41 [:out 0] (ext :cols 3 :n el))
                  (assoc-in s42 [:out 3] (ext :rows nrows :w el))
                  (assoc-in s42 [:out 2] (ext :rows nrows :e el))])]
    (mg/split-displace :x :z :offset o1 :out [s3 s3])))

(defn make-panel-seed
  [[[d h e a :as points] n] depth]
  (let [off (g/* (vec3 n) depth)
        [c g f b] (map #(g/+ off %) points)]
    (mg/seed (map vec3 [a b c d e f g h]))))

(defn make-panel
  [[[d h e a :as points] n r] tree depth]
  (let [off (g/* (vec3 n) depth)
        [c g f b] (map #(g/+ off %) points)
        seed (mg/seed (map vec3 [a b c d e f g h]))]
    (->> (mg/walk seed tree 1e6)
         (mg/union-mesh))))

(defn make-seg-panel15
  [i p]
  (let [maxy 14
        i1 (m/clamp (m/map-interval i 6 maxy 0.0225 0.05) 0.0225 0.05)
        o1 (m/map-interval i 0 maxy 0.025 0.01)
        el (m/clamp (m/map-interval i 5 maxy 0.005 0.03) 0.005 0.03)
        nr (cond
            (< i 5) 11
            (< i 8) 9
            (< i 10) 7
            (< i 13) 5
            :default 3)
        ai (m/clamp (m/map-interval i 6 maxy 0.002 0.003) 0.002 0.003)
        al (m/clamp (m/map-interval i 6 maxy 0 0.04) 0 0.03)
        leaf (when (pos? al) (make-pedals ai al))
        t (make-tree o1 0 i1 0.003 0.0025 nr el leaf)]
    (make-panel p t 0.003)))

(defn make-seg-panel13
  [i p]
  (let [maxy 12
        i1 (m/clamp (m/map-interval i 5 maxy 0.0225 0.05) 0.0225 0.05)
        o1 (m/map-interval i 0 maxy 0.025 0.01)
        el (m/clamp (m/map-interval i 5 maxy 0.005 0.03) 0.005 0.025)
        nr (cond
            (< i 4) 11
            (< i 6) 9
            (< i 8) 7
            (< i 11) 5
            :default 3)
        ai (m/clamp (m/map-interval i 4 maxy 0.002 0.003) 0.002 0.003)
        al (m/clamp (m/map-interval i 4 maxy 0 0.04) 0 0.03)
        leaf (when (pos? al) (make-pedals ai al))
        t (make-tree o1 0 i1 0.003 0.0025 nr el leaf)]
    (make-panel p t 0.003)))

(defn make-segment
  [panel-fn panels]
  (binding [m/*eps* 1e-9]
    (mg/union-mesh
     (map panel-fn (range) panels))))

(defn make-rotate-z-fn
  [^double theta]
  (let [s (Math/sin theta) c (Math/cos theta)]
    (fn [^Vec3 p]
      (Vec3. (mm/msub (.-x p) c (.-y p) s)
             (mm/madd (.-x p) s (.-y p) c)
             (.-z p)))))

(defn repeat-segments
  [segment x n]
  (let [d (/ m/TWO_PI x)
        faces (:faces segment)]
    (->> (m/norm-range x)
         (map #(* TWO_PI %))
         (drop 1)
         (take (dec n))
         (map (fn [theta]
                (prn (m/degrees theta) "°")
                (let [rfn (make-rotate-z-fn theta)]
                  (g/into (gm/gmesh) (map (fn [f] (mapv rfn f)) faces)))))
         (reduce g/into segment)
         (vector)
         (save-meshes))))

(defn export-single
  [panels make-seg i]
  (binding [*eps* 1e-9]
    (save-meshes
     (format "pseg-%d.stl" i)
     [(make-seg i (nth panels i))])))

(defn export-panels
  [panels]
  (dorun
   (map-indexed
    (fn [i [p n r]]
      (with-open [o (io/output-stream (format "p%03d.stl" i))]
        (let [p' (g/center (point-towards2 p n V3Z r V3Y))]
          (mio/write-stl o p'))))
    panels)))
