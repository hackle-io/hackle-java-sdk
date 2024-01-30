package io.hackle.sdk.core.evaluation.evaluator

import io.hackle.sdk.common.PropertiesBuilder
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.model.Experiment

object Evaluators {

    fun context(): Evaluator.Context {
        return DefaultContext()
    }

    private class DefaultContext : Evaluator.Context {

        private val _requests = mutableListOf<Evaluator.Request>()
        private val _evaluations = mutableListOf<Evaluator.Evaluation>()
        private val _properties = PropertiesBuilder()

        override val stack: List<Evaluator.Request> get() = ArrayList(_requests.toList())
        override val targetEvaluations: List<Evaluator.Evaluation> get() = ArrayList(_evaluations)
        override val properties: Map<String, Any> get() = _properties.build()

        override fun contains(request: Evaluator.Request): Boolean {
            return _requests.contains(request)
        }

        override fun add(request: Evaluator.Request) {
            _requests.add(request)
        }

        override fun remove(request: Evaluator.Request) {
            _requests.remove(request)
        }

        override fun get(experiment: Experiment): Evaluator.Evaluation? {
            return _evaluations.find { it is ExperimentEvaluation && it.experiment == experiment }
        }

        override fun add(evaluation: Evaluator.Evaluation) {
            _evaluations.add(evaluation)
        }

        override fun addProperty(key: String, value: Any?) {
            _properties.add(key, value)
        }
    }
}
