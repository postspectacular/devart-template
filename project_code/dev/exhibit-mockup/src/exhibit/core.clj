(ns exhibit.core
  "The code in this ns creates a 3d mockup scene of the exhibition space
  at the Barbican. The resulting LXS file can be rendered straight away
  with Luxrender."
  (:require
   [thi.ng.geom.core :as g]
   [thi.ng.geom.core.utils :as gu]
   [thi.ng.geom.core.vector :as v :refer [vec3 V3Y V3Z]]
   [thi.ng.geom.core.matrix :refer [M44]]
   [thi.ng.geom.core.quaternion :as quat]
   [thi.ng.geom.aabb :as a]
   [thi.ng.geom.bezier :as b]
   [thi.ng.geom.circle :as c]
   [thi.ng.geom.polygon :as p]
   [thi.ng.geom.plane :as pl]
   [thi.ng.geom.quad :as q]
   [thi.ng.geom.rect :as r]
   [thi.ng.geom.gmesh :as gm]
   [thi.ng.geom.meshio :as mio]
   [thi.ng.geom.csg.core :as csg]
   [thi.ng.luxor.core :refer :all]
   [thi.ng.luxor.io :as lio]
   [thi.ng.common.data.core :as d]
   [thi.ng.common.math.core :as m :refer [HALF_PI PI TWO_PI]]
   [clojure.java.io :as io]))

(defn make-wires
  "Takes a mesh and creates a new mesh of thin strips from each face's
  centroid to given point. Only faces with a z-centroid > thresh are
  considered."
  [canopy p thresh thick]
  (->> canopy
       (g/faces)
       (mapcat
        (fn [f]
          (let [c (gu/centroid f)
                n (vec3 (g/normalize (g/normal (:xy c)) thick))]
            (if (> (:z c) thresh)
              [[(g/- c n) (g/+ c n) (g/+ p n) (g/- p n)]]))))
       (g/into (gm/gmesh))))

(defn point-towards*
  [tx from to]
  (let [axis (gu/ortho-normal from to)
        theta (Math/acos (g/dot (g/normalize from) (g/normalize to)))
        q (quat/quat-from-axis-angle axis theta)]
    (g/transform tx q)))

(defn quad-normal
  [[a b c d]]
  (g/normalize (g/cross (g/- c a) (g/- d b))))

(defn point-towards2
  [tx from to right to-right]
  (let [axis (g/cross from to)
        theta (+ (Math/sqrt (* (g/mag-squared from) (g/mag-squared to))) (g/dot from to))
        q (g/normalize (quat/quat axis theta))
        r' (g/transform-vector q right)
        rt (+ m/PI (Math/acos (g/dot r' to-right)))
        qr (quat/quat-from-axis-angle to rt)
        ;;q (g/* qr q)
        ]
    (prn right :-> r')
    (g/transform tx q)))

(defn lathe-raw
  [points res phi rot-fn & [face-fn]]
  (let [strips (mapv
                (fn [i]
                  (let [theta (* i phi)]
                    (mapv #(let [p (rot-fn % theta)]
                             (if (m/delta= p % m/*eps*)
                               % p))
                          points)))
                (butlast (m/norm-range res)))
        strips (if (m/delta= phi m/TWO_PI)
                 (conj strips (first strips))
                 strips)
        make-face (fn [[a1 a2] [b1 b2]]
                    (let [f (cond
                             (< (count (hash-set a1 a2 b1 b2)) 3) nil
                             (= a1 b1) [b1 b2 a2]
                             (= a2 b2) [b1 a2 a1]
                             :default [b1 b2 a2 a1])]
                      (if (and f face-fn) (face-fn f) [f])))]
    (->> (d/successive-nth 2 strips)
         (mapcat
          (fn [[sa sb]]
            (map make-face
                 (d/successive-nth 2 sa)
                 (d/successive-nth 2 sb)))))))

;; circular truss, 1500mm radius, 50x50mm cross section
(def truss
  (-> (c/circle 1.2)
      (g/as-polygon 40)
      (g/extrude-shell {:depth 0.05 :wall 0.05})
      (g/transform (g/translate M44 [0 -0.5 3.025]))))

;; cross bar for truss to mount steel wires on
(def crossbar
  (let [b (-> (a/aabb [2.4 0.05 0.05]) (g/center (vec3 {:z 3.1})) (g/as-mesh))]
    (-> (g/into b (g/faces (g/transform b (-> M44 (g/rotate-z HALF_PI)))))
        (g/transform (g/translate M44 0 -0.5 0)))))

;; bounding box of 3d printer
;; uses boolean operations to carve out frame from bounding boxes
(def printer
  (let [p (-> (a/aabb [0.6 0.6 1.2]) (g/center (vec3 0 0 0.6)) (g/as-mesh))
        p1 (-> (a/aabb [0.5 0.5 1.1]) (g/center (vec3 0 0 0.6)) (g/as-mesh))
        p2 (-> (a/aabb [0.5 0.7 0.5]) (g/center (vec3 0 0 0.3)) (g/as-mesh))
        p2* (g/transform p2 (g/rotate-z M44 HALF_PI))
        p3 (-> (a/aabb [0.7 0.5 0.7]) (g/center (vec3 0 0.3 0.95)) (g/as-mesh))
        p3* (g/transform p3 (g/translate M44 0 -0.6 0))]
    (-> (->> [p p1 p2 p2* p3 p3*]
             (map csg/mesh->csg)
             (reduce csg/subtract)
             (csg/csg->mesh))
        (g/transform (g/translate M44 0 -0.5 0)))))

(def projector
  (-> (a/aabb [0.15 0.4 0.3]) (g/center (vec3 {:y -0.5 :z 0.3}))
      (g/as-mesh)))

;; 6000x4000mm
(def floor
  (-> (pl/plane V3Z 0) (g/as-mesh {:width 6.0 :height 4.0})))

;; lathe curve for panels/tiles, defined in XZ plane
(def lathe
  (let [points (mapv (comp :xzy vec3) [[0.46 0.1] [0.46 1.0] [0.7 2.75] [1.2 3.0]])]
    (conj (vec (b/sample-segment points 13)) (peek points))))

(def canopy-flat
  (-> (gm/lathe-mesh
       lathe 26 TWO_PI g/rotate-z
       #(vector (q/inset-quad % 0.003)))
      (g/transform (g/translate M44 0 -0.5 0))))

(def wires (make-wires canopy-flat (vec3 0 -0.5 3.05) 1.80 0.0005))

(def canopy
  (-> (gm/lathe-mesh
       lathe 26 TWO_PI g/rotate-z
       #(-> (q/inset-quad % 0.003)
            (q/quad3)
            (g/extrude {:offset (g/* (gu/ortho-normal (% 0) (% 1) (% 2)) 0.005)})
            (g/faces)))
      (g/transform (g/translate M44 0 -0.5 0))
      (g/tessellate)))

(def canopy-panels
  (-> (lathe-raw
       lathe 26 TWO_PI g/rotate-z
       #(let [[a b c d :as points] (q/inset-quad % 0.003) ;;(g/scale-size* 0.95 %)
              q (q/quad3 points)
              n (quad-normal points)
              r (g/normalize (g/- (g/mix a b) (g/mix c d)))]
          ;;[(g/extrude q {:offset (g/* n 0.005)}) q n r]
          [points n r]))))

(defn export-panels
  [panels]
  (dorun
   (map-indexed
    (fn [i [p n r]]
      (with-open [o (io/output-stream (format "p%03d.stl" i))]
        (let [p' (g/center (point-towards2 p n V3Z r V3Y))]
          (mio/write-stl o p'))))
    panels)))

(def p-canopy
  (let [points (mapv (comp :xzy vec3) [[0.05 0.1] [0.05 0.4] [0.1 0.78] [0.2 0.795]])
        points (conj (vec (b/sample-segment points 6)) (peek points))]
    (-> (gm/lathe-mesh
         points 6 TWO_PI g/rotate-z
         #(-> (gu/scale-size 0.95 %)
              (q/quad3)
              (g/extrude {:offset (g/* (gu/ortho-normal (% 0) (% 1) (% 2)) 0.005)})
              (g/faces)))
        (g/transform (g/rotate-z M44 (/ m/PI 6)))
        (g/tessellate))))

(def plinth-canopies
  (->> (for [i (range -1 2)]
         (-> p-canopy
             (g/transform (g/translate M44 (vec3 -1.5 (- (* i 0.75) 0.5) 0)))))
       (reduce g/into)
       (g/compute-face-normals)
       ;;(g/compute-vertex-normals)
       ))

(def p-master
  (let [base (-> (a/aabb 0.3 0.3 0.01) (g/center (vec3 0 0 0.005)) (g/as-mesh))
        pole (-> (a/aabb 0.05 0.05 0.8) (g/center (vec3 0 0 0.4)) (g/as-mesh))
        surf (-> (c/circle 0.2) (g/as-polygon 6) (g/extrude {:depth 0.005})
                 (g/transform (-> M44 (g/translate 0 0 0.8) (g/rotate-z (/ m/PI 6)))))]
    (reduce g/into [base pole surf])))

(def plinths
  (->> (for [i (range -1 2)]
         (-> p-master
             (g/transform (g/translate M44 (vec3 -1.5 (- (* i 0.75) 0.5) 0)))))
       (reduce g/into)))

(comment
  (def plinths
    (->> (for [i (range -1 2)]
           (-> (a/aabb [0.3 0.3 0.8])
               (g/center (vec3 -1.5 (- (* i 0.75) 0.5) 0.4))
               (g/as-mesh)))
         (reduce g/into))))

(def tablets
  (->> (for [i (range -1 2)]
         (-> (a/aabb (vec3 -0.13 0 0) (vec3 0.26 0.19 0.01))
             (g/as-mesh)
             (g/transform (-> M44
                              (g/translate -1.42 (- (* i 0.75) 0.5) 0.93)
                              (g/rotate-z HALF_PI)
                              (g/rotate-x (- m/QUARTER_PI))
                              )))
         )
       (reduce g/into)))

(def backwall
  (let [a1 (-> (a/aabb (vec3 -3 1.7 0) (vec3 6 0.5 6)) (g/as-mesh))
        a2 (-> (a/aabb (vec3 -2 1.6 0) (vec3 4 0.4 6)) (g/as-mesh))]
    (-> (->> [a1 a2]
             (map csg/mesh->csg)
             (reduce csg/subtract)
             (csg/csg->mesh)))))

(def shelves
  (->> (for [i (range 10)
             x (range -3 4)]
         (-> (a/aabb [0.45 0.30 0.05])
             (g/center (vec3 (* x 0.55) 1.85 (+ 0.8 (* i 0.50))))
             (g/as-mesh)))
       (reduce g/into)))

(def lcds
  (->> (for [i (range 2)
             x (range -3 4)]
         (-> (pl/plane (g/- V3Y) -1.699)
             (g/as-mesh {:p (vec3 (* x 0.55) 0 (+ 0.8 (* i 0.5))) :width 0.1 :height 0.03})))
       (reduce g/into)))

(def scene-model
  (->> [truss crossbar
        printer projector
        floor canopy wires
        plinths tablets plinth-canopies
        backwall shelves lcds
        ]
       (reduce g/into)))

(with-open [o (io/output-stream "exhibit.ply")] (mio/write-ply o scene-model))
(with-open [o (io/output-stream "exhibit.stl")] (mio/write-stl o scene-model))

;;;;; LXS

(def lxs
  (-> (lux-scene)
      (renderer-sampler)
      (sampler-ld {})
      (integrator-bidir {})
      (camera {:eye [-2.5 -4 1.8] :target [0 0.5 1.5] :up [0 0 1] :fov 50})
      (film
       {;; :width 1280 :height 720
        :width 640 :height 360
        :response :agfachrome-rsx2-200cd
        :display-interval 5 :halt-spp 1000})
      (tonemap-linear {:iso 100 :exposure 0.5 :f-stop 8 :gamma 2.2})
      (volume :glass {:type :clear :absorb [1.0 0.905 0.152] :abs-depth 0.01 :ior 1.488})
      (material-matte :white {:diffuse [0.8 0.8 0.8]})
      (material-matte :black {:diffuse [0.1 0.1 0.1]})
      (material-matte :yellow__ {:diffuse [0.8 0.8 0.5] :alpha 0.2})
      (material-glass2 :yellow {:interior :glass})
      (area-light :lcd {:mesh lcds :mesh-type :ply :color [1.0 0.13 0.08] :gain 0.1 :power 1})
      (area-light :tablets {:mesh tablets :mesh-type :ply :color [1 1 1] :gain 1 :power 1})
      (spot-light :spot {:from [0 0 0] :to [0 0 -1] :cone-angle 45 :cone-delta 5
                         :tx {:translate [0 -0.5 0.46] :rx 180}})
      (ply-mesh :metal {:mesh (reduce g/into [truss crossbar wires plinths])
                        :material :black})
      (ply-mesh :white {:mesh (reduce g/into [floor backwall shelves])
                        :material :white})
      (stl-mesh :canopy {:mesh (reduce g/into [canopy plinth-canopies]) :material :yellow})
      (ply-mesh :ilios {:mesh (reduce g/into [printer projector]) :material :white})
      (lio/serialize-scene "exhibit" false)
      (lio/export-scene)
      ))
