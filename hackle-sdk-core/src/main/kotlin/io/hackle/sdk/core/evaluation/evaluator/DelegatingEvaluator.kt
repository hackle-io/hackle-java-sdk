package io.hackle.sdk.core.evaluation.evaluator

internal class DelegatingEvaluator : Evaluator {

    private val evaluators = mutableListOf<ContextualEvaluator<*, *>>()

    fun add(evaluator: ContextualEvaluator<*, *>) {
        evaluators.add(evaluator)
    }

    override fun evaluate(request: Evaluator.Request, context: Evaluator.Context): Evaluator.Evaluation {
        val evaluator = evaluators.find { it.supports(request) }
            ?: throw IllegalArgumentException("Unsupported Evaluator.Request [${request::class.java.simpleName}]")
        return evaluator.evaluate(request, context)
    }
}
