package io.hackle.sdk.core.evaluation.evaluator.inappmessage.eligibility

import io.hackle.sdk.core.evaluation.EvaluationContext
import io.hackle.sdk.core.evaluation.flow.isDecisionWith
import io.hackle.sdk.core.evaluation.flow.isEnd
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat

class InAppMessageEligibilityFlowFactoryTest {


    @Test
    fun `flow`() {

        val evaluationContext = EvaluationContext()
        evaluationContext.initialize(mockk(), mockk(), mockk())
        val sut = InAppMessageEligibilityFlowFactory(evaluationContext, mockk())

        expectThat(sut.triggerFlow())
            .isDecisionWith<PlatformInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<OverrideInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<DraftInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<PauseInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<PeriodInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<TargetInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<LayoutResolveInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<FrequencyCapInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<HiddenInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<EligibleInAppMessageEligibilityFlowEvaluator>()
            .isEnd()

        expectThat(sut.deliverFlow(false))
            .isDecisionWith<FrequencyCapInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<HiddenInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<EligibleInAppMessageEligibilityFlowEvaluator>()
            .isEnd()

        expectThat(sut.deliverFlow(true))
            .isDecisionWith<PlatformInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<OverrideInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<DraftInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<PauseInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<PeriodInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<TargetInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<FrequencyCapInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<HiddenInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<EligibleInAppMessageEligibilityFlowEvaluator>()
            .isEnd()
    }
}
