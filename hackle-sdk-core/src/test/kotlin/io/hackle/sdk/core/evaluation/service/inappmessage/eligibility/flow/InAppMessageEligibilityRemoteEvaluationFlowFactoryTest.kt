package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow

import io.hackle.sdk.core.evaluation.service.inappmessage.InAppMessageEvaluateScope
import io.hackle.sdk.core.support.InAppMessages
import io.hackle.sdk.core.support.isEnd
import io.hackle.sdk.core.support.isStepWith
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat

internal class InAppMessageEligibilityRemoteEvaluationFlowFactoryTest {

    private val sut = InAppMessageEligibilityRemoteEvaluationFlowFactory(
        impressionStorage = mockk(),
        hiddenStorage = mockk(),
        layoutEvaluator = mockk(),
    )

    @Test
    fun `TRIGGER`() {
        val request = InAppMessages.eligibilityRemoteRequest(scope = InAppMessageEvaluateScope.TRIGGER)

        expectThat(sut.get(request))
            .isStepWith<PlatformInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<OverrideInAppMessageEligibilityRemoteFlowEvaluator>()
            .isStepWith<IneligibleInAppMessageEligibilityRemoteFlowEvaluator>()
            .isStepWith<PeriodInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<TimetableInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<LayoutResolveInAppMessageEligibilityRemoteFlowEvaluator>()
            .isStepWith<FrequencyCapInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<HiddenInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<EligibleInAppMessageEligibilityFlowEvaluator<*>>()
            .isEnd()
    }

    @Test
    fun `DELIVER`() {
        val request = InAppMessages.eligibilityRemoteRequest(
            scope = InAppMessageEvaluateScope.DELIVER,
            inAppMessage = InAppMessages.eligibilityRemoteResult(
                evaluateContext = InAppMessages.evaluateContext(atDeliverTime = false)
            )
        )

        expectThat(sut.get(request))
            .isStepWith<OverrideInAppMessageEligibilityRemoteFlowEvaluator>()
            .isStepWith<FrequencyCapInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<HiddenInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<EligibleInAppMessageEligibilityFlowEvaluator<*>>()
            .isEnd()
    }

    @Test
    fun `DELIVER 재평가`() {
        val request = InAppMessages.eligibilityRemoteRequest(
            scope = InAppMessageEvaluateScope.DELIVER,
            inAppMessage = InAppMessages.eligibilityRemoteResult(
                evaluateContext = InAppMessages.evaluateContext(atDeliverTime = true)
            )
        )

        expectThat(sut.get(request))
            .isStepWith<PlatformInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<OverrideInAppMessageEligibilityRemoteFlowEvaluator>()
            .isStepWith<IneligibleInAppMessageEligibilityRemoteFlowEvaluator>()
            .isStepWith<PeriodInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<TimetableInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<FrequencyCapInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<HiddenInAppMessageEligibilityFlowEvaluator<*>>()
            .isStepWith<EligibleInAppMessageEligibilityFlowEvaluator<*>>()
            .isEnd()
    }
}
