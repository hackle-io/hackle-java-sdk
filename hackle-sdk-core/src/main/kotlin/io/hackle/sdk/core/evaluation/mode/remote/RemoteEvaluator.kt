package io.hackle.sdk.core.evaluation.mode.remote

import io.hackle.sdk.core.evaluation.EvaluateResponse
import io.hackle.sdk.core.evaluation.evaluator.ContextualEvaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.internal.metrics.Metrics

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

            val referenceResult = request.workspace.result(reference)
            if (referenceResult == null) {
                val tags = hashMapOf(
                    "service.type" to reference.serviceType.name,
                    "entity.id" to reference.id.toString()
                )
                Metrics.counter("workspace.evaluation.reference.unresolved", tags).increment()
                log.warn { "Reference result not found (reference=$reference, root=${request.entity})" }
                continue
            }

            val referenceEvaluation = referenceResult.toEvaluation()
            context.add(referenceEvaluation)
        }
    }

    companion object {
        private val log = Logger<RemoteEvaluator<*, *>>()
    }
}
