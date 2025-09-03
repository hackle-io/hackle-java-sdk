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
        private val map = hashMapOf<Any, Any>()

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

        override fun setProperty(key: String, value: Any?) {
            _properties.add(key, value)
        }

        override fun <T> get(key: Class<T>): T? {
            val value = map[key] ?: return null
            if (key.isInstance(value)) {
                @Suppress("UNCHECKED_CAST")
                return value as T
            }
            throw NoSuchElementException("Context does not contain a value of type ${key.name}")
        }

        override fun <T> set(key: Class<T>, value: T) {
            map[key] = (value as Any)
        }
    }
}

inline fun <reified T> Evaluator.Context.get(): T? {
    return get(T::class.java)
}

inline fun <reified T> Evaluator.Context.set(value: T) {
    set(T::class.java, value)
}

