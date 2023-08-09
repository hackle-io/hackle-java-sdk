package io.hackle.sdk.core.evaluation.evaluator.inappmessage

import io.hackle.sdk.common.decision.DecisionReason.NOT_IN_IN_APP_MESSAGE_TARGET
import io.hackle.sdk.core.evaluation.evaluator.AbstractEvaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.flow.EvaluationFlowFactory

internal class InAppMessageEvaluator(
    private val evaluationFlowFactory: EvaluationFlowFactory
) : AbstractEvaluator<InAppMessageRequest, InAppMessageEvaluation>() {

    override fun supports(request: Evaluator.Request): Boolean {
        return request is InAppMessageRequest
    }

    override fun evaluateInternal(request: InAppMessageRequest, context: Evaluator.Context): InAppMessageEvaluation {
        val evaluationFlow = evaluationFlowFactory.inAppMessageFlow()
        return evaluationFlow.evaluate(request, context)
            ?: InAppMessageEvaluation.of(request, context, NOT_IN_IN_APP_MESSAGE_TARGET)
    }
}
