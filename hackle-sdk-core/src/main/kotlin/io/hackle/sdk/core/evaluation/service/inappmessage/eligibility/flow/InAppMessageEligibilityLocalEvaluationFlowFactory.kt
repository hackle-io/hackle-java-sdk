package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow

import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.evaluation.service.inappmessage.InAppMessageEvaluateScope
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.*
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.mode.local.InAppMessageEligibilityLocalEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.mode.local.InAppMessageLayoutLocalEvaluator

class InAppMessageEligibilityLocalEvaluationFlowFactory(
    targetMatcher: TargetMatcher,
    impressionStorage: InAppMessageImpressionStorage,
    hiddenStorage: InAppMessageHiddenStorage,
    layoutEvaluator: InAppMessageLayoutLocalEvaluator,
) {

    private val overrideFlow: InAppMessageEligibilityLocalEvaluationFlow =
        InAppMessageEligibilityLocalEvaluationFlow.of(
            OverrideInAppMessageEligibilityLocalFlowEvaluator(InAppMessageUserOverrideMatcher())
        )

    private val evaluateFlow: InAppMessageEligibilityLocalEvaluationFlow =
        InAppMessageEligibilityLocalEvaluationFlow.of(
            PlatformInAppMessageEligibilityLocalFlowEvaluator(),
            OverrideInAppMessageEligibilityLocalFlowEvaluator(InAppMessageUserOverrideMatcher()),
            DraftInAppMessageEligibilityLocalFlowEvaluator(),
            PauseInAppMessageEligibilityLocalFlowEvaluator(),
            PeriodInAppMessageEligibilityFlowEvaluator(),
            TimetableInAppMessageEligibilityFlowEvaluator(),
            TargetInAppMessageEligibilityLocalFlowEvaluator(InAppMessageTargetMatcher(targetMatcher)),
        )

    private val layoutFlow: InAppMessageEligibilityLocalEvaluationFlow = InAppMessageEligibilityLocalEvaluationFlow.of(
        LayoutResolveInAppMessageEligibilityLocalFlowEvaluator(layoutEvaluator)
    )

    private val deduplicateFlow: InAppMessageEligibilityLocalEvaluationFlow =
        InAppMessageEligibilityLocalEvaluationFlow.of(
            FrequencyCapInAppMessageEligibilityFlowEvaluator(InAppMessageFrequencyCapMatcher(impressionStorage)),
            HiddenInAppMessageEligibilityFlowEvaluator(InAppMessageHiddenMatcher(hiddenStorage)),
        )

    private val eligibleFlow: InAppMessageEligibilityLocalEvaluationFlow =
        InAppMessageEligibilityLocalEvaluationFlow.of(
            EligibleInAppMessageEligibilityFlowEvaluator()
        )

    private val triggerFlow: InAppMessageEligibilityLocalEvaluationFlow =
        evaluateFlow + layoutFlow + deduplicateFlow + eligibleFlow

    private val deliverFlow: InAppMessageEligibilityLocalEvaluationFlow = overrideFlow + deduplicateFlow + eligibleFlow
    private val deliverReEvaluateFlow: InAppMessageEligibilityLocalEvaluationFlow =
        evaluateFlow + deduplicateFlow + eligibleFlow


    fun get(request: InAppMessageEligibilityLocalEvaluateRequest): InAppMessageEligibilityLocalEvaluationFlow {
        return when (request.scope) {
            InAppMessageEvaluateScope.TRIGGER -> triggerFlow
            InAppMessageEvaluateScope.DELIVER -> if (request.entity.evaluateContext.atDeliverTime) deliverReEvaluateFlow else deliverFlow
        }
    }
}
