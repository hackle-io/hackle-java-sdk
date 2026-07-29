package io.hackle.sdk.core.evaluation.evaluator

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.Entity

object Evaluators {

    fun context(): Evaluator.Context {
        return DefaultContext()
    }

    private class DefaultContext : Evaluator.Context {

        private val _requests = mutableListOf<EvaluateRequest>()
        private val _evaluations = mutableListOf<Evaluation>()

        override val stack: List<EvaluateRequest> get() = ArrayList(_requests.toList())
        override val references: List<Evaluation> get() = ArrayList(_evaluations)
        private val map = hashMapOf<Any, Any>()

        override fun contains(request: EvaluateRequest): Boolean {
            return _requests.contains(request)
        }

        override fun add(request: EvaluateRequest) {
            _requests.add(request)
        }

        override fun remove(request: EvaluateRequest) {
            _requests.remove(request)
        }

        override fun get(entity: Entity): Evaluation? {
            return _evaluations.find { it.entity == entity }
        }

        override fun add(evaluation: Evaluation) {
            _evaluations.add(evaluation)
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
