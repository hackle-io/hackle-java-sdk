package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.mode.local

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.set
import io.hackle.sdk.core.evaluation.event.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.flow.EvaluationFlow
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow.InAppMessageEligibilityLocalEvaluationFlowFactory
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.InAppMessages
import io.hackle.sdk.core.support.create
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.startsWith

@ExtendWith(MockKExtension::class)
internal class InAppMessageEligibilityLocalEvaluatorTest {

    @MockK
    private lateinit var evaluationFlowFactory: InAppMessageEligibilityLocalEvaluationFlowFactory

    @RelaxedMockK
    private lateinit var eventRecorder: EvaluationEventRecorder

    @InjectMockKs
    private lateinit var sut: InAppMessageEligibilityLocalEvaluator

    @Test
    fun `supports - InAppMessageEligibilityLocalEvaluateRequest 만 지원한다`() {
        assertTrue(sut.supports(InAppMessages.eligibilityLocalRequest()))
        assertFalse(sut.supports(InAppMessages.eligibilityRemoteRequest()))
    }

    @Nested
    inner class EvaluateTest {

        @Test
        fun `순환 호출이 발생하면 예외 발생`() {
            // given
            val request = InAppMessages.eligibilityLocalRequest()
            val context = Evaluators.context()
            context.add(request)

            // when
            val exception = assertThrows<IllegalArgumentException> { sut.evaluate(request, context) }

            // then
            expectThat(exception.message)
                .isNotNull()
                .startsWith("Circular evaluation has occurred")
        }

        @Test
        fun `flow 평가 결과로 응답을 생성한다`() {
            // given
            val result = InAppMessages.eligibilityResult()
            every { evaluationFlowFactory.get(any()) } returns EvaluationFlow.create(result)

            val request = InAppMessages.eligibilityLocalRequest()

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

            val request = InAppMessages.eligibilityLocalRequest()

            // when
            val actual = sut.evaluate(request, Evaluators.context())

            // then
            expectThat(actual) {
                get { evaluation.result.reason } isEqualTo DecisionReason.NOT_IN_IN_APP_MESSAGE_TARGET
                get { evaluation.result.isEligible }.isFalse()
            }
        }

        @Test
        fun `context 에 추가된 reference 평가를 response 에 포함한다`() {
            // given
            val result = InAppMessages.eligibilityResult()
            every { evaluationFlowFactory.get(any()) } returns EvaluationFlow.create(result)

            val referenceEvaluation = Experiments.evaluation()
            val context = Evaluators.context()
            context.add(referenceEvaluation)

            // when
            val actual = sut.evaluate(InAppMessages.eligibilityLocalRequest(), context)

            // then
            expectThat(actual) {
                get { references }.hasSize(1)
                get { references[0] } isSameInstanceAs referenceEvaluation
            }
        }

        @Test
        fun `context 에 추가된 layout 평가를 response 에 포함한다`() {
            // given
            val result = InAppMessages.eligibilityResult()
            every { evaluationFlowFactory.get(any()) } returns EvaluationFlow.create(result)

            val layoutResponse = InAppMessages.layoutResponse()
            val context = Evaluators.context()
            context.set(layoutResponse)

            // when
            val actual = sut.evaluate(InAppMessages.eligibilityLocalRequest(), context)

            // then
            expectThat(actual) {
                get { layout } isSameInstanceAs layoutResponse
            }
        }
    }

    @Nested
    inner class RecordTest {

        @Test
        fun `eligible 이면 layout 은 기록하지 않는다`() {
            // given
            val layoutResponse = InAppMessages.layoutResponse()
            val response = InAppMessages.eligibilityResponse(
                evaluation = InAppMessages.eligibilityEvaluation(
                    result = InAppMessages.eligibilityResult(isEligible = true)
                ),
                layout = layoutResponse
            )

            // when
            sut.record(InAppMessages.eligibilityLocalRequest(), response)

            // then
            verify(exactly = 1) { eventRecorder.record(any()) }
            verify(exactly = 1) { eventRecorder.record(response) }
        }

        @Test
        fun `ineligible 이지만 layout 이 없으면 eligibility 만 기록한다`() {
            // given
            val response = InAppMessages.eligibilityResponse(
                evaluation = InAppMessages.eligibilityEvaluation(
                    result = InAppMessages.eligibilityResult(isEligible = false)
                ),
                layout = null
            )

            // when
            sut.record(InAppMessages.eligibilityLocalRequest(), response)

            // then
            verify(exactly = 1) { eventRecorder.record(any()) }
        }

        @Test
        fun `ineligible 이고 layout 이 있으면 layout 도 기록한다`() {
            // given
            val layoutResponse = InAppMessages.layoutResponse()
            val response = InAppMessages.eligibilityResponse(
                evaluation = InAppMessages.eligibilityEvaluation(
                    result = InAppMessages.eligibilityResult(isEligible = false)
                ),
                layout = layoutResponse
            )

            // when
            sut.record(InAppMessages.eligibilityLocalRequest(), response)

            // then
            verify(exactly = 1) { eventRecorder.record(response) }
            verify(exactly = 1) { eventRecorder.record(layoutResponse) }
        }
    }
}
