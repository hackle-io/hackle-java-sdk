package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow

import io.hackle.sdk.core.evaluation.EvaluationPhase
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

    // Source Flow

    private val platformFlow: InAppMessageEligibilityLocalEvaluationFlow =
        InAppMessageEligibilityLocalEvaluationFlow.of(
            PlatformInAppMessageEligibilityLocalFlowEvaluator(),
        )

    private val overrideFlow: InAppMessageEligibilityLocalEvaluationFlow =
        InAppMessageEligibilityLocalEvaluationFlow.of(
            OverrideInAppMessageEligibilityLocalFlowEvaluator(InAppMessageUserOverrideMatcher())
        )

    private val statusFlow: InAppMessageEligibilityLocalEvaluationFlow =
        InAppMessageEligibilityLocalEvaluationFlow.of(
            DraftInAppMessageEligibilityLocalFlowEvaluator(),
            PauseInAppMessageEligibilityLocalFlowEvaluator(),
        )

    private val timeFlow: InAppMessageEligibilityLocalEvaluationFlow =
        InAppMessageEligibilityLocalEvaluationFlow.of(
            PeriodInAppMessageEligibilityFlowEvaluator(),
            TimetableInAppMessageEligibilityFlowEvaluator(),
        )

    private val targetFlow: InAppMessageEligibilityLocalEvaluationFlow =
        InAppMessageEligibilityLocalEvaluationFlow.of(
            TargetInAppMessageEligibilityLocalFlowEvaluator(InAppMessageTargetMatcher(targetMatcher)),
        )

    private val layoutFlow: InAppMessageEligibilityLocalEvaluationFlow = InAppMessageEligibilityLocalEvaluationFlow.of(
        LayoutResolveInAppMessageEligibilityLocalFlowEvaluator(layoutEvaluator)
    )

    private val dedupFlow: InAppMessageEligibilityLocalEvaluationFlow =
        InAppMessageEligibilityLocalEvaluationFlow.of(
            FrequencyCapInAppMessageEligibilityFlowEvaluator(InAppMessageFrequencyCapMatcher(impressionStorage)),
            HiddenInAppMessageEligibilityFlowEvaluator(InAppMessageHiddenMatcher(hiddenStorage)),
        )

    private val eligibleFlow: InAppMessageEligibilityLocalEvaluationFlow =
        InAppMessageEligibilityLocalEvaluationFlow.of(
            EligibleInAppMessageEligibilityFlowEvaluator()
        )

    // Runtime Flow

    private val triggerFlow: InAppMessageEligibilityLocalEvaluationFlow =
        InAppMessageEligibilityLocalEvaluationFlow.concat(
            platformFlow,
            overrideFlow,
            statusFlow,
            timeFlow,
            targetFlow,
            layoutFlow,
            dedupFlow,
            eligibleFlow
        )

    private val deliverFlow: InAppMessageEligibilityLocalEvaluationFlow =
        InAppMessageEligibilityLocalEvaluationFlow.concat(
            overrideFlow,
            dedupFlow,
            eligibleFlow
        )

    private val deliverReEvaluateFlow: InAppMessageEligibilityLocalEvaluationFlow =
        InAppMessageEligibilityLocalEvaluationFlow.concat(
            platformFlow,
            overrideFlow,
            statusFlow,
            timeFlow,
            targetFlow,
            dedupFlow,
            eligibleFlow
        )

    // Sync Flow

    private val syncFlow: InAppMessageEligibilityLocalEvaluationFlow =
        InAppMessageEligibilityLocalEvaluationFlow.concat(
            layoutFlow,
            platformFlow,
            overrideFlow,
            statusFlow,
            targetFlow,
            eligibleFlow,
        )

    fun get(request: InAppMessageEligibilityLocalEvaluateRequest): InAppMessageEligibilityLocalEvaluationFlow {
        return when (request.phase) {
            EvaluationPhase.SYNC -> syncFlow
            EvaluationPhase.RUNTIME -> when (request.scope) {
                InAppMessageEvaluateScope.TRIGGER -> triggerFlow
                InAppMessageEvaluateScope.DELIVER -> if (request.entity.evaluateContext.atDeliverTime) deliverReEvaluateFlow else deliverFlow
            }
        }
    }
}
