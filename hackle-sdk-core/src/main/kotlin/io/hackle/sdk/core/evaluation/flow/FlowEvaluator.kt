package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.core.evaluation.evaluator.Evaluator.Context
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentRequest

/**
 * @author Yong
 */
internal interface FlowEvaluator {
    fun evaluate(
        request: ExperimentRequest,
        context: Context,
        nextFlow: EvaluationFlow
    ): ExperimentEvaluation
}
