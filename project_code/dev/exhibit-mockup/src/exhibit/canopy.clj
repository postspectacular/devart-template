(ns exhibit.canopy
  (:require
   [exhibit.utils :refer :all]
   [thi.ng.geom.core :as g]
   [thi.ng.geom.core.utils :as gu]
   [thi.ng.geom.core.vector :as v :refer [vec3 V3Y V3Z]]
   [thi.ng.geom.core.matrix :refer [M44]]
   [thi.ng.geom.bezier :as b]
   [thi.ng.geom.polygon :as p]
   [thi.ng.geom.quad :as q]
   [thi.ng.geom.gmesh :as gm]
   [thi.ng.common.data.core :as d]
   [thi.ng.common.math.core :as m :refer [HALF_PI PI TWO_PI]]))

;; lathe curve for panels/tiles, defined in XZ plane
(def lathe
  (let [points (mapv (comp :xzy vec3) [[0.46 0.1] [0.46 1.0] [0.7 2.75] [1.2 3.0]])]
    (conj (vec (b/sample-segment points 13)) (peek points))))

(def canopy-flat
  (-> (gm/lathe-mesh
       lathe 26 TWO_PI g/rotate-z
       #(vector (q/inset-quad % 0.003)))
      (g/transform (g/translate M44 0 -0.5 0))))

;;(def wires (make-wires canopy-flat (vec3 0 -0.5 3.05) 1.80 0.0005))

(def canopy
  (-> (gm/lathe-mesh
       lathe 26 TWO_PI g/rotate-z
       (inset-and-extrude-quad 0.003 0.005))
      (g/transform (g/translate M44 0 -0.5 0))
      (g/tessellate)))

(def canopy-panels
  (-> (lathe-raw
       lathe 26 TWO_PI g/rotate-z
       #(let [[a b c d :as points] (q/inset-quad % 0.003) ;;(g/scale-size* 0.95 %)
              q (q/quad3 points)
              n (quad-normal points)
              r (g/normalize (g/- (g/mix a b) (g/mix c d)))]
          [points n r]))))
