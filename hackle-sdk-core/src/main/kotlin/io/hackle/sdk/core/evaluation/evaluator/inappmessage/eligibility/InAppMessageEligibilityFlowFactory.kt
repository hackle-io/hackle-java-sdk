package io.hackle.sdk.core.evaluation.evaluator.inappmessage.eligibility

import io.hackle.sdk.core.evaluation.EvaluationContext
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.layout.InAppMessageLayoutEvaluator
import io.hackle.sdk.core.evaluation.get

class InAppMessageEligibilityFlowFactory(context: EvaluationContext, layoutEvaluator: InAppMessageLayoutEvaluator) {

    private val evaluateFlow: InAppMessageEligibilityFlow = InAppMessageEligibilityFlow.of(
        PlatformInAppMessageEligibilityFlowEvaluator(),
        OverrideInAppMessageEligibilityFlowEvaluator(context.get()),
        DraftInAppMessageEligibilityFlowEvaluator(),
        PauseInAppMessageEligibilityFlowEvaluator(),
        PeriodInAppMessageEligibilityFlowEvaluator(),
        TargetInAppMessageEligibilityFlowEvaluator(context.get()),
    )

    private val layoutFlow: InAppMessageEligibilityFlow = InAppMessageEligibilityFlow.of(
        LayoutResolveInAppMessageEligibilityFlowEvaluator(layoutEvaluator)
    )

    private val deduplicateFlow: InAppMessageEligibilityFlow = InAppMessageEligibilityFlow.of(
        FrequencyCapInAppMessageEligibilityFlowEvaluator(context.get()),
        HiddenInAppMessageEligibilityFlowEvaluator(context.get()),
    )

    private val eligibleFlow: InAppMessageEligibilityFlow = InAppMessageEligibilityFlow.of(
        EligibleInAppMessageEligibilityFlowEvaluator()
    )

    private val triggerFlow: InAppMessageEligibilityFlow = evaluateFlow + layoutFlow + deduplicateFlow + eligibleFlow

    private val deliverFlow: InAppMessageEligibilityFlow = deduplicateFlow + eligibleFlow
    private val deliverReEvaluateFlow: InAppMessageEligibilityFlow = evaluateFlow + deduplicateFlow + eligibleFlow


    fun triggerFlow(): InAppMessageEligibilityFlow {
        return triggerFlow
    }

    fun deliverFlow(reEvaluate: Boolean): InAppMessageEligibilityFlow {
        return if (reEvaluate) deliverReEvaluateFlow else deliverFlow
    }
}
