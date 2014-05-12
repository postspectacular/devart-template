(ns exhibit.panelgen2
  (:require
   [exhibit.utils :refer :all]
   [exhibit.canopy :refer :all]
   [exhibit.plinths :refer :all]
   [exhibit.svg :as svg]
   [thi.ng.geom.core :as g]
   [thi.ng.geom.core.utils :as gu]
   [thi.ng.geom.core.vector :as v :refer [vec3 V3X V3Y V3Z]]
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
  [o1 o2 i1 i2 i3 i4 ncols nrows el leaf]
  (let [inner (fn [id i l2]
                (let [l1 (if (pos? i) (mg/subdiv-inset :dir :z :inset i :out {4 nil}) {})]
                  (mg/subdiv id 3 :out [l2 l1 l2])))
        s41 (mg/subdiv-inset
             :dir :z :inset i2
             :out {4 (inner :cols i3 (inner :rows i4 nil))})
        s42 (mg/subdiv-inset
             :dir :z :inset i2
             :out {4 (inner :rows i3 (inner :cols i4 nil))})
        ext (fn [dir1 n dir2 elen]
              (mg/extrude
               :dir :f :len 0.005
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
            :out [(assoc-in s41 [:out 1] (ext :cols ncols :s el))
                  (assoc-in s41 [:out 0] (ext :cols ncols :n el))
                  (assoc-in s42 [:out 3] (ext :rows nrows :w el))
                  (assoc-in s42 [:out 2] (ext :rows nrows :e el))])]
    (mg/split-displace :x :z :offset o1 :out [s3 s3])))

(comment
  (mg/subdiv-inset
   :dir :z :inset i1
   :out {4 nil}))

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
  [flat?]
  (fn [i p]
    (let [maxy 14
          i1 (m/map-interval-clamped i 6 maxy 0.0225 0.05)
          i2 0.003
          i3 0.0025
          i4 (* i3 0.5)
          o1 (if flat? 0 (m/map-interval i 0 maxy 0.025 0.01))
          el (m/map-interval-clamped i 5 maxy 0.005 0.03)
          nr (cond
              (< i 5) 11
              (< i 8) 9
              (< i 10) 7
              (< i 13) 5
              :default 3)
          ai (m/map-interval-clamped i 6 maxy 0.002 0.003)
          al (m/map-interval-clamped i 6 maxy 0 0.04 0 0.03)
          leaf (when (pos? al) (make-pedals ai al))
          t (make-tree o1 0 i1 i2 i3 i4 3 nr el leaf)]
      (make-panel p t 0.003))))

(defn make-seg-panel13
  [flat?]
  (fn [i p]
    (let [maxy 12
          i1 (m/map-interval-clamped i 5 maxy 0.0225 0.05)
          i2 0.003
          i3 0.0025
          i4 (* i3 0.5)
          o1 (if flat? 0 (m/map-interval i 0 maxy 0.025 0.01))
          el (m/map-interval-clamped i 5 maxy 0.005 0.03 0.005 0.025)
          nr (cond
              (< i 4) 11
              (< i 6) 9
              (< i 8) 7
              (< i 11) 5
              :default 3)
          ai (m/map-interval-clamped i 4 maxy 0.002 0.003)
          al (m/map-interval-clamped i 4 maxy 0 0.04 0 0.03)
          leaf (when (pos? al) (make-pedals ai al))
          t (make-tree o1 0 i1 i2 i3 i4 3 nr el leaf)]
      (make-panel p t 0.003))))

(defn make-seg-panel6
  [flat?]
  (fn [i p]
    (let [maxy 5
          i1 ([0.009 0.011 0.013 0.0175 0.02 0.02] i)
          i2 (m/map-interval-clamped i 0 maxy 0.001 0.0025)
          i3 (m/map-interval-clamped i 0 maxy 0.001 0.002)
          i4 (* i3 0.75)
          o1 (if flat? 0 (m/map-interval i 0 maxy 0.01 0.005))
          el (m/map-interval-clamped i 2 maxy 0.005 0.015)
          nc ([3 3 3 3 5 7] i)
          nr ([11 11 9 7 5 5] i)
          ai (m/map-interval-clamped i 2 maxy 0.0005 0.001)
          al (m/map-interval-clamped i 2 maxy 0 0.005)
          leaf (when (pos? al) (make-pedals ai al))
          t (make-tree o1 0 i1 i2 i3 i4 nc nr el leaf)]
      (make-panel p t 0.003))))

(defn make-segment
  [panel-fn panels]
  (binding [m/*eps* 1e-9]
    (mg/union-mesh
     (map panel-fn (range) panels))))

(defn make-rotated-segment
  [panel-fn panels res i]
  (-> (make-segment panel-fn panels)
      (g/transform (g/rotate-z M44 (* i (/ TWO_PI res))))))

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

(defn make-segment-individual-meshes
  [panel-fn panels]
  (binding [m/*eps* 1e-9]
    (mapv
     (fn [i [p n r :as panel]]
       (let [pmesh (panel-fn i panel)
             pmesh (point-towards3 pmesh (g/centroid pmesh) n V3Z V3X)
             tx (g/translate M44 0 0 (- (first (gu/axis-bounds 2 (keys (:vertices pmesh))))))
             pmesh (g/transform pmesh tx)]
         pmesh))
     (range) panels)))

(defn export-panels
  [pmeshes]
  (dorun
   (map-indexed
    (fn [i pm]
      (with-open [o (io/output-stream (format "p%03d.stl" i))]
        (mio/write-stl o pm)))
    pmeshes)))

(defn select-mesh-slice
  [zrange m]
  (gm/map-faces
   (fn [f]
     (let [c (:z (gu/centroid f))
           d (g/dot V3Z (gu/ortho-normal f))]
       ;;(when (m/in-range? zrange c) (prn c d))
       (if (and (m/in-range? zrange c) (m/delta= 1.0 d 0.1))
         [f])))
   m))

(defn mesh->svg
  [m]
  (->> (g/center (g/transform m (g/scale M44 1000)) (vec3 100 100 0))
       (g/faces)
       (map #(svg/path {:fill "black"} %))
       (svg/svg {:width "200mm" :height "200mm" :viewBox "0 0 200 200"})
       (svg/->xml)))

(defn mesh->svg-paths
  [pos m]
  (->> (g/center (g/transform m (g/scale M44 1000)) pos)
       (g/faces)
       (map #(svg/path {:fill "black"} %))
       (vector :g {})))

(defn export-mesh-slices-svg
  [path m]
  (->> {1 [0 0.0048] 2 [0.005 0.007] 3 [0.0075 0.0095]}
       (map
        (fn [[i zr]]
          (->> m
               (select-mesh-slice zr)
               (mesh->svg-paths (vec3 (* i 100) 100 0)))))
       (svg/svg {:width "400mm" :height "200mm" :viewBox "0 0 400 200"})
       (svg/->xml)
       (spit path)))

(defn export-segment-slices-svg
  [seg-id meshes]
  (dorun
   (map-indexed
    (fn [i m]
      (export-mesh-slices-svg (str "seg-" seg-id "-" i ".svg") m))
    meshes)))
