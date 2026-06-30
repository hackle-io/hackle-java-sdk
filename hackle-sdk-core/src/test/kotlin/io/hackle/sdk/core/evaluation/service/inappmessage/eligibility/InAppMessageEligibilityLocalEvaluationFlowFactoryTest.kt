package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility

import io.hackle.sdk.core.HackleContext
import io.hackle.sdk.core.evaluation.flow.isDecisionWith
import io.hackle.sdk.core.evaluation.flow.isEnd
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow.DraftInAppMessageEligibilityFlowEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow.EligibleInAppMessageEligibilityFlowEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow.FrequencyCapInAppMessageEligibilityFlowEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow.HiddenInAppMessageEligibilityFlowEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow.InAppMessageEligibilityLocalEvaluationFlowFactory
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow.LayoutResolveInAppMessageEligibilityFlowEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow.OverrideInAppMessageEligibilityFlowEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow.PauseInAppMessageEligibilityFlowEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow.PeriodInAppMessageEligibilityFlowEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow.PlatformInAppMessageEligibilityFlowEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow.TargetInAppMessageEligibilityFlowEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow.TimetableInAppMessageEligibilityFlowEvaluator
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat

class InAppMessageEligibilityLocalEvaluationFlowFactoryTest {


    @Test
    fun `flow`() {

        val evaluationContext = HackleContext()
        evaluationContext.initialize(mockk(), mockk(), mockk())
        val sut = InAppMessageEligibilityLocalEvaluationFlowFactory(evaluationContext, mockk())

        expectThat(sut.triggerFlow())
            .isDecisionWith<PlatformInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<OverrideInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<DraftInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<PauseInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<PeriodInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<TimetableInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<TargetInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<LayoutResolveInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<FrequencyCapInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<HiddenInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<EligibleInAppMessageEligibilityFlowEvaluator>()
            .isEnd()

        expectThat(sut.deliverFlow(false))
            .isDecisionWith<OverrideInAppMessageEligibilityFlowEvaluator>()
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
            .isDecisionWith<TimetableInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<TargetInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<FrequencyCapInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<HiddenInAppMessageEligibilityFlowEvaluator>()
            .isDecisionWith<EligibleInAppMessageEligibilityFlowEvaluator>()
            .isEnd()
    }
}
