package io.hackle.sdk.core.evaluation.evaluator.inappmessage.eligibility

import io.hackle.sdk.common.decision.DecisionReason.NOT_IN_IN_APP_MESSAGE_TARGET
import io.hackle.sdk.core.evaluation.evaluator.ContextualEvaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.flow.EvaluationFlowFactory

class InAppMessageEligibilityEvaluator(
    private val evaluationFlowFactory: EvaluationFlowFactory,
) : ContextualEvaluator<InAppMessageEligibilityRequest, InAppMessageEligibilityEvaluation>() {

    override fun supports(request: Evaluator.Request): Boolean {
        return request is InAppMessageEligibilityRequest
    }

    override fun evaluateInternal(
        request: InAppMessageEligibilityRequest,
        context: Evaluator.Context,
    ): InAppMessageEligibilityEvaluation {
        val evaluationFlow = evaluationFlowFactory.inAppMessageFlow()
        return evaluationFlow.evaluate(request, context)
            ?: InAppMessageEligibilityEvaluation.ineligible(request, context, NOT_IN_IN_APP_MESSAGE_TARGET)
    }
}
