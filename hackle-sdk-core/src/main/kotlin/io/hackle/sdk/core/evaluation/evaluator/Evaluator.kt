package io.hackle.sdk.core.evaluation.evaluator

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.EvaluateResponse
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.Entity

interface Evaluator<REQUEST : EvaluateRequest, RESPONSE : EvaluateResponse> {

    fun evaluate(request: REQUEST, context: Context): RESPONSE
    fun record(request: REQUEST, response: RESPONSE)

    interface Context {
        val stack: List<EvaluateRequest>
        val references: List<Evaluation>

        operator fun contains(request: EvaluateRequest): Boolean
        fun add(request: EvaluateRequest)
        fun remove(request: EvaluateRequest)

        operator fun get(entity: Entity): Evaluation?
        fun add(evaluation: Evaluation)

        operator fun <T> get(key: Class<T>): T?
        operator fun <T> set(key: Class<T>, value: T)
    }
}
