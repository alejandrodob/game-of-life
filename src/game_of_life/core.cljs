(ns game-of-life.core
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [clojure.set :as set-ops]
              [goog.dom :as dom]
              [goog.events :as events]
              [cljs.core.async :as async :refer [put! chan <! >! timeout]]))


(enable-console-print!)

(def step-interval 200)
(def start-btn (dom/getElement "start"))
(def step-btn (dom/getElement "step"))
(def pause-btn (dom/getElement "pause"))
(def canvas (dom/getElement "grid"))
(def canvas-ctx (.getContext canvas "2d"))
(def world-width 50)
(def world-height 50)
(def life-probability 0.5)
(def canvas-width (.-width canvas))
(def canvas-height (.-height canvas))
(def cell-size (min (/ canvas-width world-width) (/ canvas-height world-height)))
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

(defn neighbours [[x y]]
  (set
    (for [i [-1 0 1]
          j [-1 0 1]
          :when (not= 0 i j)]
      [(+ x i) (+ y j)])))

(defn alive-neighbours [cell living-cells]
  (set-ops/intersection (neighbours cell) living-cells))

(defn lives-next-gen? [cell living-cells]
  (let [neighbours-count (count (alive-neighbours cell living-cells))]
    (or
      (and (contains? living-cells cell) (contains? #{2, 3} neighbours-count))
      (and (not (contains? living-cells cell)) (= neighbours-count 3)))))

(defn next-generation [living-cells]
  (set
    (for [x (range world-width)
          y (range world-height)
          :when (lives-next-gen? [x y] living-cells)]
      [x y])))

(defn paint-cell [[x y]]
  (let [color "#FE853E"]
    (do
      (set! (.-fillStyle canvas-ctx) color)
      (.fillRect canvas-ctx (* cell-size x) (* cell-size y) (dec cell-size) (dec cell-size)))))

(defn paint-cells [living-cells]
  (set! (.-fillStyle canvas-ctx) "white")
  (.fillRect canvas-ctx 0 0 canvas-width canvas-height)
  (doseq [cell living-cells]
    (paint-cell cell)))

(defn async-loop [world-state app-state]
  (go
    (while true
      (let [current-state @world-state]
        (<! @app-state)
        (paint-cells current-state)
        (reset! world-state (next-generation current-state))
        (>! @app-state "running")
        (<! (timeout step-interval))))))

(defn listen [el type]
  (let [c (chan)]
    (events/listen el type #(put! c %))
    c))

(defn listen-to-click [element]
  (listen element "click"))

(def initial-state (generate-random-living-cells life-probability))
(def world-state (atom initial-state))
(def app-state (atom (chan 1)))

(go
  (>! @app-state "running"))

(go
  (while true
    (<! (listen-to-click pause-btn))
    (let [state-chan @app-state
          current-state (<! state-chan)]
      (<! (listen-to-click pause-btn))
      (>! state-chan current-state))))

(go
  (while true
    (<! (listen-to-click start-btn))
    (async-loop world-state app-state)))

(go
  (while true
    (<! (listen-to-click step-btn))
    (let [current-state @world-state]
      (paint-cells current-state)
      (reset! world-state (next-generation current-state)))))
