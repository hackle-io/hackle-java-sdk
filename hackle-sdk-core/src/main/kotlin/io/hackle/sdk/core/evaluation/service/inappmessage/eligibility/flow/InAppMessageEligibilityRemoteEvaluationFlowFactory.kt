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


    private val platformFlow: InAppMessageEligibilityRemoteEvaluationFlow =
        InAppMessageEligibilityRemoteEvaluationFlow.of(
            PlatformInAppMessageEligibilityFlowEvaluator()
        )

    private val overrideFlow: InAppMessageEligibilityRemoteEvaluationFlow =
        InAppMessageEligibilityRemoteEvaluationFlow.of(
            OverrideInAppMessageEligibilityRemoteFlowEvaluator()
        )

    private val ineligibleFlow: InAppMessageEligibilityRemoteEvaluationFlow =
        InAppMessageEligibilityRemoteEvaluationFlow.of(
            IneligibleInAppMessageEligibilityRemoteFlowEvaluator()
        )

    private val timeFlow: InAppMessageEligibilityRemoteEvaluationFlow =
        InAppMessageEligibilityRemoteEvaluationFlow.of(
            PeriodInAppMessageEligibilityFlowEvaluator(),
            TimetableInAppMessageEligibilityFlowEvaluator(),
        )

    private val layoutFlow: InAppMessageEligibilityRemoteEvaluationFlow =
        InAppMessageEligibilityRemoteEvaluationFlow.of(
            LayoutResolveInAppMessageEligibilityRemoteFlowEvaluator(layoutEvaluator)
        )

    private val dedupFlow: InAppMessageEligibilityRemoteEvaluationFlow =
        InAppMessageEligibilityRemoteEvaluationFlow.of(
            FrequencyCapInAppMessageEligibilityFlowEvaluator(InAppMessageFrequencyCapMatcher(impressionStorage)),
            HiddenInAppMessageEligibilityFlowEvaluator(InAppMessageHiddenMatcher(hiddenStorage)),
        )

    private val eligibleFlow: InAppMessageEligibilityRemoteEvaluationFlow =
        InAppMessageEligibilityRemoteEvaluationFlow.of(
            EligibleInAppMessageEligibilityFlowEvaluator()
        )

    // Runtime Flow

    private val triggerFlow: InAppMessageEligibilityRemoteEvaluationFlow =
        InAppMessageEligibilityRemoteEvaluationFlow.concat(
            platformFlow,
            overrideFlow,
            ineligibleFlow,
            timeFlow,
            layoutFlow,
            dedupFlow,
            eligibleFlow
        )

    private val deliverFlow: InAppMessageEligibilityRemoteEvaluationFlow =
        InAppMessageEligibilityRemoteEvaluationFlow.concat(
            overrideFlow,
            dedupFlow,
            eligibleFlow
        )
    private val deliverReEvaluationFlow: InAppMessageEligibilityRemoteEvaluationFlow =
        InAppMessageEligibilityRemoteEvaluationFlow.concat(
            platformFlow,
            overrideFlow,
            ineligibleFlow,
            timeFlow,
            dedupFlow,
            eligibleFlow
        )

    fun get(request: InAppMessageEligibilityRemoteEvaluateRequest): InAppMessageEligibilityRemoteEvaluationFlow {
        return when (request.scope) {
            InAppMessageEvaluateScope.TRIGGER -> triggerFlow
            InAppMessageEvaluateScope.DELIVER -> if (request.entity.evaluateContext.atDeliverTime) deliverReEvaluationFlow else deliverFlow
        }
    }
}
