package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.mode.local

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.event.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.mode.local.LocalEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateResponse
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateResult
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow.InAppMessageEligibilityLocalEvaluationFlowFactory

class InAppMessageEligibilityLocalEvaluator(
    private val evaluationFlowFactory: InAppMessageEligibilityLocalEvaluationFlowFactory,
    private val eventRecorder: EvaluationEventRecorder,
) : LocalEvaluator<InAppMessageEligibilityLocalEvaluateRequest, InAppMessageEligibilityEvaluateResponse>(),
    InAppMessageEligibilityEvaluator<InAppMessageEligibilityLocalEvaluateRequest> {
    override fun supports(request: EvaluateRequest): Boolean {
        return request is InAppMessageEligibilityLocalEvaluateRequest
    }

    override fun doEvaluate(
        request: InAppMessageEligibilityLocalEvaluateRequest,
        context: Evaluator.Context,
    ): InAppMessageEligibilityEvaluateResponse {
        val evaluationFlow = evaluationFlowFactory.get(request)
        val result = evaluationFlow.evaluate(request, context)
            ?: InAppMessageEligibilityEvaluateResult.ineligible(DecisionReason.NOT_IN_IN_APP_MESSAGE_TARGET)
        return InAppMessageEligibilityEvaluateResponse.of(request, context, result)
    }

    override fun record(
        request: InAppMessageEligibilityLocalEvaluateRequest,
        response: InAppMessageEligibilityEvaluateResponse,
    ) {
        eventRecorder.record(response)
        if (!response.evaluation.result.isEligible && response.layout != null) {
            eventRecorder.record(response.layout)
        }
    }
}
