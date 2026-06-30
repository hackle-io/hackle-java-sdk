package io.hackle.sdk.core.evaluation.evaluator

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.EvaluateResponse

class DelegatingEvaluator(
    private val evaluatorFactory: EvaluatorFactory,
) : Evaluator<EvaluateRequest, EvaluateResponse> {
    override fun evaluate(request: EvaluateRequest, context: Evaluator.Context): EvaluateResponse {
        val evaluator = evaluatorFactory.get(request)
        return evaluator.evaluate(request, context)
    }

    override fun record(request: EvaluateRequest, response: EvaluateResponse) {
        val evaluator = evaluatorFactory.get(request)
        evaluator.record(request, response)
    }
}
