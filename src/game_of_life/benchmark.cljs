(ns game-of-life.benchmark
  (:require [game-of-life.core :as gol]))

;;(let [ex-time
;;      (time (dotimes [n 100]
;;              (let [state @gol/world-state]
;;                (gol/paint-cells state)
;;                (reset! gol/world-state (gol/next-generation state)))))]
;;  (println ex-time))
