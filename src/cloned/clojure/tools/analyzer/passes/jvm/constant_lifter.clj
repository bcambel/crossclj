(ns cloned.clojure.tools.analyzer.passes.jvm.constant-lifter
  (:require [cloned.clojure.tools.analyzer.passes.constant-lifter :as orig]
            [cloned.clojure.tools.analyzer :refer [analyze-const]]
            [cloned.clojure.tools.analyzer.utils :refer [constant? classify]]
            [cloned.clojure.tools.analyzer.passes.jvm.analyze-host-expr :refer [analyze-host-expr]]
            [cloned.clojure.tools.analyzer.passes.elide-meta :refer [elide-meta]]))

(defn constant-lift*
  [ast]
  (if (= :var (:op ast))
    (let [{:keys [var env form meta]} ast]
     (if (constant? var meta)
       (let [val @var]
         (assoc (analyze-const val env (classify val))
           :form form))
       ast))
    (orig/constant-lift ast)))

(defn constant-lift
  "Like cloned.clojure.tools.analyzer.passes.constant-lifter/constant-lift but
   transforms also :var nodes where the var has :const in the metadata
   into :const nodes and preserves tag info"
  {:pass-info {:walk :post :depends #{} :after #{#'elide-meta #'analyze-host-expr}}}
  [ast]
  (merge (constant-lift* ast)
         (select-keys ast [:tag :o-tag :return-tag :arglists])))
