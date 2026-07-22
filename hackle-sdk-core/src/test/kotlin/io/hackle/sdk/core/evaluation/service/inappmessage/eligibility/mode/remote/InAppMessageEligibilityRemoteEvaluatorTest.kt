package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.mode.remote

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.event.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.flow.EvaluationFlow
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow.InAppMessageEligibilityRemoteEvaluationFlowFactory
import io.hackle.sdk.core.model.DefaultEntity
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.InAppMessages
import io.hackle.sdk.core.support.Workspaces
import io.hackle.sdk.core.support.create
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isSameInstanceAs

@ExtendWith(MockKExtension::class)
internal class InAppMessageEligibilityRemoteEvaluatorTest {

    @MockK
    private lateinit var evaluationFlowFactory: InAppMessageEligibilityRemoteEvaluationFlowFactory

    @RelaxedMockK
    private lateinit var eventRecorder: EvaluationEventRecorder

    @InjectMockKs
    private lateinit var sut: InAppMessageEligibilityRemoteEvaluator

    @Test
    fun `supports - InAppMessageEligibilityRemoteEvaluateRequest 만 지원한다`() {
        assertTrue(sut.supports(InAppMessages.eligibilityRemoteRequest()))
        assertFalse(sut.supports(InAppMessages.eligibilityLocalRequest()))
    }

    @Test
    fun `flow 평가 결과로 응답을 생성한다`() {
        // given
        val result = InAppMessages.eligibilityResult()
        every { evaluationFlowFactory.get(any()) } returns EvaluationFlow.create(result)

        val request = InAppMessages.eligibilityRemoteRequest()

        // when
        val actual = sut.evaluate(request, Evaluators.context())

        // then
        expectThat(actual) {
            get { evaluation.entity } isSameInstanceAs request.entity
            get { evaluation.result } isSameInstanceAs result
        }
    }

    @Test
    fun `flow 평가 결과가 없으면 NOT_IN_IN_APP_MESSAGE_TARGET, ineligible 로 평가한다`() {
        // given
        every { evaluationFlowFactory.get(any()) } returns EvaluationFlow.end()

        val request = InAppMessages.eligibilityRemoteRequest()

        // when
        val actual = sut.evaluate(request, Evaluators.context())

        // then
        expectThat(actual) {
            get { evaluation.result.reason } isEqualTo DecisionReason.NOT_IN_IN_APP_MESSAGE_TARGET
            get { evaluation.result.isEligible }.isFalse()
        }
    }

    @Test
    fun `reference 평가 결과를 response 에 포함한다`() {
        // given
        val result = InAppMessages.eligibilityResult()
        every { evaluationFlowFactory.get(any()) } returns EvaluationFlow.create(result)

        val referenced = Experiments.remoteResult()
        val inAppMessage = InAppMessages.eligibilityRemoteResult(
            references = listOf(DefaultEntity(referenced.serviceType, referenced.id))
        )
        val workspace = Workspaces.evaluation(
            experiments = listOf(referenced),
            inAppMessages = listOf(inAppMessage)
        )
        val request = InAppMessages.eligibilityRemoteRequest(workspace = workspace, inAppMessage = inAppMessage)

        // when
        val actual = sut.evaluate(request, Evaluators.context())

        // then
        expectThat(actual) {
            get { references }.hasSize(1)
            get { references[0].entity } isSameInstanceAs referenced
        }
    }

    @Test
    fun `record - eligible 이면 layout 은 기록하지 않는다`() {
        // given
        val layoutResponse = InAppMessages.layoutResponse()
        val response = InAppMessages.eligibilityResponse(
            evaluation = InAppMessages.eligibilityEvaluation(
                result = InAppMessages.eligibilityResult(isEligible = true)
            ),
            layout = layoutResponse
        )

        // when
        sut.record(InAppMessages.eligibilityRemoteRequest(), response)

        // then
        verify(exactly = 1) { eventRecorder.record(any()) }
        verify(exactly = 1) { eventRecorder.record(response) }
    }

    @Test
    fun `record - ineligible 이고 layout 이 있으면 layout 도 기록한다`() {
        // given
        val layoutResponse = InAppMessages.layoutResponse()
        val response = InAppMessages.eligibilityResponse(
            evaluation = InAppMessages.eligibilityEvaluation(
                result = InAppMessages.eligibilityResult(isEligible = false)
            ),
            layout = layoutResponse
        )

        // when
        sut.record(InAppMessages.eligibilityRemoteRequest(), response)

        // then
        verify(exactly = 1) { eventRecorder.record(response) }
        verify(exactly = 1) { eventRecorder.record(layoutResponse) }
    }
}
