package io.hackle.sdk.core.evaluation.evaluator

import io.hackle.sdk.core.evaluation.evaluator.Evaluator.*

internal abstract class AbstractEvaluator<in REQUEST : Request, out EVALUATION : Evaluation> : Evaluator {

    abstract fun supports(request: Request): Boolean

    protected abstract fun evaluateInternal(request: REQUEST, context: Context): EVALUATION

    @Suppress("UNCHECKED_CAST")
    final override fun evaluate(request: Request, context: Context): EVALUATION {
        return evaluateInternal(request as REQUEST, context)
    }
}
