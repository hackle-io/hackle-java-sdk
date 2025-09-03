package io.hackle.sdk.core.evaluation.evaluator.inappmessage.eligibility

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.model.InAppMessages
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.*

@ExtendWith(MockKExtension::class)
internal class InAppMessageEligibilityEvaluatorTest {

    @MockK
    private lateinit var evaluationFlow: InAppMessageEligibilityFlow

    @RelaxedMockK
    private lateinit var eventRecorder: EvaluationEventRecorder

    @InjectMockKs
    private lateinit var sut: InAppMessageEligibilityEvaluator

    @Test
    fun `supports`() {
        expectThat(sut.supports(experimentRequest())).isFalse()
        expectThat(sut.supports(InAppMessages.eligibilityRequest())).isTrue()
    }

    @Nested
    inner class EvaluateTest {

        @Test
        fun `circular`() {
            val request = InAppMessages.eligibilityRequest()
            val context = Evaluators.context()
            context.add(request)

            val exception = assertThrows<IllegalArgumentException> { sut.evaluate(request, context) }

            expectThat(exception.message)
                .isNotNull()
                .startsWith("Circular evaluation has occurred")
        }

        @Test
        fun `flow - evaluation`() {
            // given
            val evaluation = InAppMessages.eligibilityEvaluation()
            every { evaluationFlow.evaluate(any(), any()) } returns evaluation

            val request = InAppMessages.eligibilityRequest()
            val context = Evaluators.context()

            // when
            val actual = sut.evaluate(request, context)

            // then
            expectThat(actual).isSameInstanceAs(evaluation)
        }

        @Test
        fun `flow - default`() {
            // given
            every { evaluationFlow.evaluate(any(), any()) } returns null

            val request = InAppMessages.eligibilityRequest()
            val context = Evaluators.context()

            // when
            val actual = sut.evaluate(request, context)

            // then
            expectThat(actual.reason) isEqualTo DecisionReason.NOT_IN_IN_APP_MESSAGE_TARGET
        }
    }

    @Nested
    inner class RecordTest {
        @Test
        fun `record EligibilityEvaluation`() {
            // given
            val request = InAppMessages.eligibilityRequest()
            val evaluation = InAppMessages.eligibilityEvaluation()

            // when
            sut.record(request, evaluation)

            // then
            verify(exactly = 1) {
                eventRecorder.record(request, evaluation)
            }
        }

        @Test
        fun `when eligible then do not record layout evaluation`() {
            // given
            val request = InAppMessages.eligibilityRequest()
            val layoutEvaluation = InAppMessages.layoutEvaluation()
            val evaluation = InAppMessages.eligibilityEvaluation(
                isEligible = true,
                layoutEvaluation = layoutEvaluation
            )

            // when
            sut.record(request, evaluation)

            // then
            verify(exactly = 0) {
                eventRecorder.record(any(), layoutEvaluation)
            }
        }

        @Test
        fun `when ineligible with no layout then do not record`() {
            // given
            val request = InAppMessages.eligibilityRequest()
            val evaluation = InAppMessages.eligibilityEvaluation(
                isEligible = false,
                layoutEvaluation = null
            )

            // when
            sut.record(request, evaluation)

            // then
            verify(exactly = 1) {
                eventRecorder.record(any(), any())
            }
        }

        @Test
        fun `when ineligible with layout then record layout evaluation`() {
            // given
            val request = InAppMessages.eligibilityRequest()
            val layoutEvaluation = InAppMessages.layoutEvaluation()
            val evaluation = InAppMessages.eligibilityEvaluation(
                isEligible = false,
                layoutEvaluation = layoutEvaluation
            )

            // when
            sut.record(request, evaluation)

            // then
            verify(exactly = 1) {
                eventRecorder.record(request, evaluation)
            }
            verify(exactly = 1) {
                eventRecorder.record(layoutEvaluation.request, layoutEvaluation)
            }
        }
    }
}
