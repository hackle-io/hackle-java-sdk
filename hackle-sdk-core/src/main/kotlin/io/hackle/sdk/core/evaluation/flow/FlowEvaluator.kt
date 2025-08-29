package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.core.evaluation.evaluator.Evaluator

/**
 * @author Yong
 */
interface FlowEvaluator<REQUEST : Evaluator.Request, EVALUATION : Evaluator.Evaluation> {
    fun evaluate(
        request: REQUEST,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow<REQUEST, EVALUATION>,
    ): EVALUATION?
}
