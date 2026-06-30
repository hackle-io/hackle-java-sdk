package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow

import io.hackle.sdk.core.evaluation.service.inappmessage.InAppMessageEvaluateScope
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.InAppMessageFrequencyCapMatcher
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.InAppMessageHiddenMatcher
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.InAppMessageHiddenStorage
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.InAppMessageImpressionStorage
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.mode.remote.InAppMessageEligibilityRemoteEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.mode.remote.InAppMessageLayoutRemoteEvaluator

class InAppMessageEligibilityRemoteEvaluationFlowFactory(
    impressionStorage: InAppMessageImpressionStorage,
    hiddenStorage: InAppMessageHiddenStorage,
    layoutEvaluator: InAppMessageLayoutRemoteEvaluator,
) {

    private val overrideFlow: InAppMessageEligibilityRemoteEvaluationFlow =
        InAppMessageEligibilityRemoteEvaluationFlow.of(
            OverrideInAppMessageEligibilityRemoteFlowEvaluator()
        )

    private val evaluationFlow: InAppMessageEligibilityRemoteEvaluationFlow =
        InAppMessageEligibilityRemoteEvaluationFlow.of(
            PlatformInAppMessageEligibilityRemoteFlowEvaluator(),
            OverrideInAppMessageEligibilityRemoteFlowEvaluator(),
            IneligibleInAppMessageEligibilityRemoteFlowEvaluator(),
            PeriodInAppMessageEligibilityFlowEvaluator(),
            TimetableInAppMessageEligibilityFlowEvaluator(),
        )

    private val layoutFlow: InAppMessageEligibilityRemoteEvaluationFlow =
        InAppMessageEligibilityRemoteEvaluationFlow.of(
            LayoutResolveInAppMessageEligibilityRemoteFlowEvaluator(layoutEvaluator)
        )

    private val deduplicateFlow: InAppMessageEligibilityRemoteEvaluationFlow =
        InAppMessageEligibilityRemoteEvaluationFlow.of(
            FrequencyCapInAppMessageEligibilityFlowEvaluator(InAppMessageFrequencyCapMatcher(impressionStorage)),
            HiddenInAppMessageEligibilityFlowEvaluator(InAppMessageHiddenMatcher(hiddenStorage)),
        )

    private val eligibleFlow: InAppMessageEligibilityRemoteEvaluationFlow =
        InAppMessageEligibilityRemoteEvaluationFlow.of(
            EligibleInAppMessageEligibilityFlowEvaluator()
        )

    private val triggerFlow: InAppMessageEligibilityRemoteEvaluationFlow =
        evaluationFlow + layoutFlow + deduplicateFlow + eligibleFlow

    private val deliverFlow: InAppMessageEligibilityRemoteEvaluationFlow = overrideFlow + deduplicateFlow + eligibleFlow
    private val deliverReEvaluationFlow: InAppMessageEligibilityRemoteEvaluationFlow =
        evaluationFlow + deduplicateFlow + eligibleFlow

    fun get(request: InAppMessageEligibilityRemoteEvaluateRequest): InAppMessageEligibilityRemoteEvaluationFlow {
        return when (request.scope) {
            InAppMessageEvaluateScope.TRIGGER -> triggerFlow
            InAppMessageEvaluateScope.DELIVER -> if (request.entity.evaluateContext.atDeliverTime) deliverReEvaluationFlow else deliverFlow
        }
    }
}
