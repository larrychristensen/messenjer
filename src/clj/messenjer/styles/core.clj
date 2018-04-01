(ns messenjer.styles.core
     (:require [garden.def :refer [defstylesheet defstyles]]
               [garden.units :refer [px]]
               [garden.selectors :as s]))

(defn props [prefix css-kw values]
  (mapv
   (fn [v]
     [(keyword (str "." prefix "-" v)) {css-kw (px v)}])
   values))

(defn box-prop [prefix css-kw values]
  (mapcat
   (fn [[p kw]]
     (props p kw values))
   [[prefix css-kw]
    [(str prefix "-l") (keyword (str (name css-kw) "-left"))]
    [(str prefix "-r") (keyword (str (name css-kw) "-right"))]
    [(str prefix "-t") (keyword (str (name css-kw) "-top"))]
    [(str prefix "-b") (keyword (str (name css-kw) "-bottom"))]]))

(def widths (props "w" :width [220]))

(def font-sizes (props "f-s" :font-size (range 8 40)))

(def paddings (box-prop "p" :padding (range 1 40)))

(def margins (box-prop "m" :margin (range 1 40)))

(def gray "rgb(202,196,201)")

(def green "#4C9689")

(def app
  (concat
   [[:html :body :#app {:height "100%"}]
    [:body {:margin 0}]
    [:#app {:font-family "sans-serif"}]
    [:.flex {:display :flex}]
    [:.flex-grow-1 {:flex-grow 1}]
    [:.flex-column {:flex-direction :column}]
    [:.flex-column-reverse {:flex-direction :column-reverse}]
    [:.align-items-center {:align-items :center}]
    [:.h-100-p {:height "100%"}]
    [:.bg-purple {:background-color "#4d394b"}]
    [:.bg-green {:background-color green}]
    [:.green {:color green}]
    [:.white {:color :white}]
    [:.gray {:color gray}]
    [:.dark-gray {:color "rgb(100, 100, 100)"}]
    [:.bold {:font-weight :bold}]
    [:.pointer {:cursor :pointer}]
    [:.overflow-auto {:overflow :auto}]
    [:.button
     {:border-radius "8px"
      :border :none
      :color :white
      :height "30px"
      :background-color green
      :cursor :pointer}]
    [:.input
     {:background-color :transparent
      :border (str "2px solid " gray)
      :border-radius "8px"
      :margin-top "5px"
      :display :block
      :padding "12px"
      :width "100%"
      :box-sizing :border-box
      :font-size "14px"}]]
   widths
   font-sizes
   paddings
   margins))
