package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.set
import io.hackle.sdk.core.evaluation.flow.EvaluationFlow
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateResult
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.mode.remote.InAppMessageEligibilityRemoteEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.mode.remote.InAppMessageLayoutRemoteEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.mode.remote.InAppMessageLayoutRemoteEvaluator
import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.model.supports

typealias InAppMessageEligibilityRemoteEvaluationFlow = EvaluationFlow<InAppMessageEligibilityRemoteEvaluateRequest, InAppMessageEligibilityEvaluateResult>

interface InAppMessageEligibilityRemoteFlowEvaluator :
    InAppMessageEligibilityFlowEvaluator<InAppMessageEligibilityRemoteEvaluateRequest> {
    override fun evaluate(
        request: InAppMessageEligibilityRemoteEvaluateRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityRemoteEvaluationFlow,
    ): InAppMessageEligibilityEvaluateResult?
}

class PlatformInAppMessageEligibilityRemoteFlowEvaluator : InAppMessageEligibilityRemoteFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityRemoteEvaluateRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityRemoteEvaluationFlow,
    ): InAppMessageEligibilityEvaluateResult? {
        if (!request.inAppMessage.supports(InAppMessage.PlatformType.ANDROID)) {
            return InAppMessageEligibilityEvaluateResult.ineligible(DecisionReason.UNSUPPORTED_PLATFORM)
        }
        if (request.entity.reason == DecisionReason.UNSUPPORTED_PLATFORM) {
            return InAppMessageEligibilityEvaluateResult.ineligible(DecisionReason.UNSUPPORTED_PLATFORM)
        }
        return nextFlow.evaluate(request, context)
    }
}

class OverrideInAppMessageEligibilityRemoteFlowEvaluator : InAppMessageEligibilityRemoteFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityRemoteEvaluateRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityRemoteEvaluationFlow,
    ): InAppMessageEligibilityEvaluateResult? {
        if (request.entity.reason == DecisionReason.OVERRIDDEN) {
            return InAppMessageEligibilityEvaluateResult.eligible(DecisionReason.OVERRIDDEN)
        }
        return nextFlow.evaluate(request, context)
    }
}

class IneligibleInAppMessageEligibilityRemoteFlowEvaluator : InAppMessageEligibilityRemoteFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityRemoteEvaluateRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityRemoteEvaluationFlow,
    ): InAppMessageEligibilityEvaluateResult? {
        if (!request.entity.isEligible) {
            return InAppMessageEligibilityEvaluateResult.ineligible(request.entity.reason)
        }

        return nextFlow.evaluate(request, context)
    }
}

class LayoutResolveInAppMessageEligibilityRemoteFlowEvaluator(
    private val layoutEvaluator: InAppMessageLayoutRemoteEvaluator,
) : InAppMessageEligibilityFlowEvaluator<InAppMessageEligibilityRemoteEvaluateRequest> {
    override fun evaluate(
        request: InAppMessageEligibilityRemoteEvaluateRequest,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow<InAppMessageEligibilityRemoteEvaluateRequest, InAppMessageEligibilityEvaluateResult>,
    ): InAppMessageEligibilityEvaluateResult? {
        val layoutRequest = InAppMessageLayoutRemoteEvaluateRequest.of(request, request.inAppMessage.layout)
        val layoutResponse = layoutEvaluator.evaluate(layoutRequest, Evaluators.context())
        context.set(layoutResponse)

        return nextFlow.evaluate(request, context)
    }
}
