package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.EvaluateResult
import io.hackle.sdk.core.evaluation.evaluator.Evaluator

interface FlowEvaluator<REQUEST : EvaluateRequest, RESULT : EvaluateResult> {
    fun evaluate(
        request: REQUEST,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow<REQUEST, RESULT>,
    ): RESULT?
}
