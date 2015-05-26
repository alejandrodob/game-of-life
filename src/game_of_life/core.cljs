(ns game-of-life.core
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [clojure.set :as set-ops]
              [goog.dom :as dom]
              [goog.events :as events]
              [cljs.core.async :refer [put! chan <!]]
              [cljs.core.async :refer [chan put! take! timeout] :as async]))


(enable-console-print!)

(def create-btn (dom/getElement "create"))
(def canvas (dom/getElement "grid"))
(def canvas-ctx (.getContext canvas "2d"))
(def world-width 50)
(def world-height 50)
(def canvas-width (.-width canvas))
(def canvas-height (.-height canvas))
(def cell-size 10)
(def colors ["#E16889"
             "#FE853E"
             "#6EC59B"
             "#FDBA52"
             "#F5DED0"
             "#94614C"
             "#2D97D3"
             "#48C3CB"
             "#A9A6D3"
             "#C0C1BC"])

(defn generate-random-living-cells [probability]
  (set
    (for [x (range world-width)
          y (range world-height)
          :when (< (rand) probability)]
      [x y])))

(defn neighbours [cell]
  (let [[x y] cell]
    (set
      (for [i [-1 0 1]
            j [-1 0 1]
            :when (not= 0 i j)]
        [(+ x i) (+ y j)]))))

(defn alive-neighbours [cell living-cells]
  (set-ops/intersection (neighbours cell) living-cells))

(defn lives-next-gen? [cell living-cells]
  (let [neighbour-count (count (alive-neighbours cell living-cells))]
    (or
      (and (contains? living-cells cell) (contains? #{2, 3} neighbour-count))
      (and (not (contains? living-cells cell)) (= neighbour-count 3)))))

(defn next-generation [living-cells]
  (set
    (for [x (range world-width)
          y (range world-height)
          :when (lives-next-gen? [x y] living-cells)]
      [x y])))

(defn paint-cell [cell alive?]
  (let [[x y] cell
        color (if alive? "#FE853E" "white")]
    (do
      (set! (.-fillStyle canvas-ctx) color)
      (.fillRect canvas-ctx (* cell-size x) (* cell-size y) (dec cell-size) (dec cell-size)))))

(defn paint-cells [living-cells]
  ;; mejorar esto, en lugar de pintar TODAS las cells, pintar todo el canvas de blanco y luego colorear sólo las vivas
  (dotimes [x world-width]
    (dotimes [y world-height]
      (let [alive? (contains? living-cells [x y])]
        (paint-cell [x y] alive?)))))


(def initial-state (generate-random-living-cells 0.5))
(def world-state (atom initial-state))

(defn main-loop [world-state]
  (js/setInterval
    #(let [current-state @world-state]
      (do
        (paint-cells current-state)
        (reset! world-state (next-generation current-state))))
    500))

(main-loop world-state)

;;(paint-cells initial-state)
;;(println (count initial-state))
;;(println (count (next-generation initial-state)))
;;(paint-cells (next-generation initial-state))
;;(js/setTimeout #(paint-cells (next-generation initial-state)) 1000)
