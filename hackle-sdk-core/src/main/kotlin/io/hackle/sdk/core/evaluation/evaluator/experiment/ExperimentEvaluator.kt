package io.hackle.sdk.core.evaluation.evaluator.experiment

import io.hackle.sdk.core.evaluation.evaluator.AbstractEvaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.flow.EvaluationFlowFactory

internal class ExperimentEvaluator(
    private val evaluationFlowFactory: EvaluationFlowFactory
) : AbstractEvaluator<ExperimentRequest, ExperimentEvaluation>() {
    override fun supports(request: Evaluator.Request): Boolean {
        return request is ExperimentRequest
    }

    override fun evaluateInternal(request: ExperimentRequest, context: Evaluator.Context): ExperimentEvaluation {
        val evaluationFlow = evaluationFlowFactory.getFlow(request.experiment.type)
        return evaluationFlow.evaluate(request, context)
    }
}