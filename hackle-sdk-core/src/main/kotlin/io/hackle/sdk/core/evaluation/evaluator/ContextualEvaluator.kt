package io.hackle.sdk.core.evaluation.evaluator

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.EvaluateResponse

abstract class ContextualEvaluator<REQUEST : EvaluateRequest, RESPONSE : EvaluateResponse> :
    Evaluator<REQUEST, RESPONSE> {
    abstract fun supports(request: EvaluateRequest): Boolean
    protected abstract fun doEvaluate(request: REQUEST, context: Evaluator.Context): RESPONSE
    final override fun evaluate(request: REQUEST, context: Evaluator.Context): RESPONSE {
        require(request !in context) { "Circular evaluation has occurred [${(context.stack + request).joinToString(" - ")}]" }
        context.add(request)
        return try {
            doEvaluate(request, context)
        } finally {
            context.remove(request)
        }
    }
}
