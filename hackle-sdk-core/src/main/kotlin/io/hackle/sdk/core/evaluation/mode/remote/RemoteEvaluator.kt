package io.hackle.sdk.core.evaluation.mode.remote

import io.hackle.sdk.core.evaluation.EvaluateResponse
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.evaluator.ContextualEvaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.workspace.evaluation.entity.RemoteEvaluateResult

abstract class RemoteEvaluator<REQUEST : RemoteEvaluateRequest, RESPONSE : EvaluateResponse> :
    ContextualEvaluator<REQUEST, RESPONSE>() {

    protected abstract fun remoteEvaluate(request: REQUEST, context: Evaluator.Context): RESPONSE

    final override fun doEvaluate(request: REQUEST, context: Evaluator.Context): RESPONSE {
        resolveReferences(request, context)
        return remoteEvaluate(request, context)
    }

    private fun resolveReferences(request: REQUEST, context: Evaluator.Context) {
        for (reference in request.entity.references) {
            if (context[reference] != null) continue
            val referenceResult = request.workspace.result(reference) ?: continue
            val evaluation = resolveReference(request, referenceResult)
            context.add(evaluation)
        }
    }

    protected open fun resolveReference(request: REQUEST, result: RemoteEvaluateResult): Evaluation {
        return result.toEvaluation()
    }
}
