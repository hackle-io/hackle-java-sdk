package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow

import io.hackle.sdk.core.evaluation.EvaluationPhase
import io.hackle.sdk.core.evaluation.service.inappmessage.InAppMessageEvaluateScope
import io.hackle.sdk.core.support.InAppMessages
import io.hackle.sdk.core.support.isEnd
import io.hackle.sdk.core.support.isStepWith
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat

internal class InAppMessageEligibilityLocalEvaluationFlowFactoryTest {

    private val sut = InAppMessageEligibilityLocalEvaluationFlowFactory(
        targetMatcher = mockk(),
        impressionStorage = mockk(),
        hiddenStorage = mockk(),
        layoutEvaluator = mockk(),
    )

    @Test
    fun `SYNC`() {
        val request = InAppMessages.eligibilityLocalRequest(phase = EvaluationPhase.SYNC)

        expectThat(sut.get(request))
            .isStepWith<LayoutResolveInAppMessageEligibilityLocalFlowEvaluator>()
            .isStepWith<OverrideInAppMessageEligibilityLocalFlowEvaluator>()
            .isStepWith<DraftInAppMessageEligibilityLocalFlowEvaluator>()
            .isStepWith<PauseInAppMessageEligibilityLocalFlowEvaluator>()
            .isStepWith<TargetInAppMessageEligibilityLocalFlowEvaluator>()
            .isStepWith<EligibleInAppMessageEligibilityFlowEvaluator<*>>()
            .isEnd()
    }

    @Test
    fun `RUNTIME - TRIGGER`() {
        val request = InAppMessages.eligibilityLocalRequest(scope = InAppMessageEvaluateScope.TRIGGER)

        expectThat(sut.get(request))
            .isStepWith<PlatformInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<OverrideInAppMessageEligibilityLocalFlowEvaluator>()
            .isStepWith<DraftInAppMessageEligibilityLocalFlowEvaluator>()
            .isStepWith<PauseInAppMessageEligibilityLocalFlowEvaluator>()
            .isStepWith<PeriodInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<TimetableInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<TargetInAppMessageEligibilityLocalFlowEvaluator>()
            .isStepWith<LayoutResolveInAppMessageEligibilityLocalFlowEvaluator>()
            .isStepWith<FrequencyCapInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<HiddenInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<EligibleInAppMessageEligibilityFlowEvaluator<*>>()
            .isEnd()
    }

    @Test
    fun `RUNTIME - DELIVER`() {
        val request = InAppMessages.eligibilityLocalRequest(
            scope = InAppMessageEvaluateScope.DELIVER,
            inAppMessage = InAppMessages.config(
                evaluateContext = InAppMessages.evaluateContext(atDeliverTime = false)
            )
        )

        expectThat(sut.get(request))
            .isStepWith<OverrideInAppMessageEligibilityLocalFlowEvaluator>()
            .isStepWith<FrequencyCapInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<HiddenInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<EligibleInAppMessageEligibilityFlowEvaluator<*>>()
            .isEnd()
    }

    @Test
    fun `RUNTIME - DELIVER 재평가`() {
        val request = InAppMessages.eligibilityLocalRequest(
            scope = InAppMessageEvaluateScope.DELIVER,
            inAppMessage = InAppMessages.config(
                evaluateContext = InAppMessages.evaluateContext(atDeliverTime = true)
            )
        )

        expectThat(sut.get(request))
            .isStepWith<PlatformInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<OverrideInAppMessageEligibilityLocalFlowEvaluator>()
            .isStepWith<DraftInAppMessageEligibilityLocalFlowEvaluator>()
            .isStepWith<PauseInAppMessageEligibilityLocalFlowEvaluator>()
            .isStepWith<PeriodInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<TimetableInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<TargetInAppMessageEligibilityLocalFlowEvaluator>()
            .isStepWith<FrequencyCapInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<HiddenInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<EligibleInAppMessageEligibilityFlowEvaluator<*>>()
            .isEnd()
    }
}
