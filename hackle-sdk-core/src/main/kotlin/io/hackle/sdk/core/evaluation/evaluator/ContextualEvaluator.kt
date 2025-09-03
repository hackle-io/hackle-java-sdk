package io.hackle.sdk.core.evaluation.evaluator

import io.hackle.sdk.core.evaluation.evaluator.Evaluator.*

abstract class ContextualEvaluator<in REQUEST : Request, out EVALUATION : Evaluation> : Evaluator {

    abstract fun supports(request: Request): Boolean
    protected abstract fun evaluateInternal(request: REQUEST, context: Context): EVALUATION

    final override fun evaluate(request: Request, context: Context): EVALUATION {
        require(request !in context) { "Circular evaluation has occurred [${(context.stack + request).joinToString(" - ")}]" }
        context.add(request)
        return try {
            @Suppress("UNCHECKED_CAST")
            evaluateInternal(request as REQUEST, context)
        } finally {
            context.remove(request)
        }
    }
}
