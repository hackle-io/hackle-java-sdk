package io.hackle.sdk.core.evaluation.evaluator

import io.hackle.sdk.core.internal.log.Logger

internal class DelegatingEvaluator : Evaluator {

    private val evaluators = mutableListOf<AbstractEvaluator<*, *>>()

    fun add(evaluator: AbstractEvaluator<*, *>) {
        evaluators.add(evaluator)
        log.info { "Evaluator added [${evaluator::class.java.simpleName}]" }
    }

    override fun evaluate(request: Evaluator.Request, context: Evaluator.Context): Evaluator.Evaluation {
        val evaluator = evaluators.find { it.supports(request) }
            ?: throw IllegalArgumentException("Unsupported Evaluator.Request [${request::class.java.simpleName}]")
        return evaluator.evaluate(request, context)
    }

    companion object {
        private val log = Logger<DelegatingEvaluator>()
    }
}
