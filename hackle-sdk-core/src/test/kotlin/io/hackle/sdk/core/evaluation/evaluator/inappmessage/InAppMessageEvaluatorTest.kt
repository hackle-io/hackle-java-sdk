package io.hackle.sdk.core.evaluation.evaluator.inappmessage

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.evaluation.flow.EvaluationFlow
import io.hackle.sdk.core.evaluation.flow.EvaluationFlowFactory
import io.hackle.sdk.core.evaluation.flow.create
import io.hackle.sdk.core.model.InAppMessages
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.*

@ExtendWith(MockKExtension::class)
internal class InAppMessageEvaluatorTest {

    @MockK
    private lateinit var evaluationFlowFactory: EvaluationFlowFactory

    @InjectMockKs
    private lateinit var sut: InAppMessageEvaluator

    @Test
    fun `supports`() {
        expectThat(sut.supports(experimentRequest())).isFalse()
        expectThat(sut.supports(InAppMessages.request())).isTrue()
    }

    @Nested
    inner class EvaluateTest {

        @Test
        fun `circular`() {
            val request = InAppMessages.request()
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
            val evaluation = InAppMessages.evaluation()
            val evaluationFlow: InAppMessageFlow = EvaluationFlow.create(evaluation)
            every { evaluationFlowFactory.inAppMessageFlow() } returns evaluationFlow

            val request = InAppMessages.request()
            val context = Evaluators.context()

            // when
            val actual = sut.evaluate(request, context)

            // then
            expectThat(actual).isSameInstanceAs(evaluation)
        }

        @Test
        fun `flow - default`() {
            // given
            val evaluationFlow: InAppMessageFlow = EvaluationFlow.end()
            every { evaluationFlowFactory.inAppMessageFlow() } returns evaluationFlow

            val request = InAppMessages.request()
            val context = Evaluators.context()

            // when
            val actual = sut.evaluate(request, context)

            // then
            expectThat(actual.reason) isEqualTo DecisionReason.NOT_IN_IN_APP_MESSAGE_TARGET
        }
    }
}
