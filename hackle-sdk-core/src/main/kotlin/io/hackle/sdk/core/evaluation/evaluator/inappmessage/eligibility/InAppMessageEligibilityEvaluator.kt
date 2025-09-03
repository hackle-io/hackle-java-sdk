package io.hackle.sdk.core.evaluation.evaluator.inappmessage.eligibility

import io.hackle.sdk.common.decision.DecisionReason.NOT_IN_IN_APP_MESSAGE_TARGET
import io.hackle.sdk.core.evaluation.evaluator.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.InAppMessageEvaluator

class InAppMessageEligibilityEvaluator(
    private val evaluationFlow: InAppMessageEligibilityFlow,
    private val eventRecorder: EvaluationEventRecorder,
) : InAppMessageEvaluator<InAppMessageEligibilityRequest, InAppMessageEligibilityEvaluation>() {

    override fun supports(request: Evaluator.Request): Boolean {
        return request is InAppMessageEligibilityRequest
    }

    override fun evaluateInternal(
        request: InAppMessageEligibilityRequest,
        context: Evaluator.Context,
    ): InAppMessageEligibilityEvaluation {
        return evaluationFlow.evaluate(request, context)
            ?: InAppMessageEligibilityEvaluation.ineligible(request, context, NOT_IN_IN_APP_MESSAGE_TARGET)
    }

    override fun record(request: InAppMessageEligibilityRequest, evaluation: InAppMessageEligibilityEvaluation) {
        eventRecorder.record(request, evaluation)
        if (!evaluation.isEligible && evaluation.layoutEvaluation != null) {
            eventRecorder.record(evaluation.layoutEvaluation.request, evaluation.layoutEvaluation)
        }
    }
}
