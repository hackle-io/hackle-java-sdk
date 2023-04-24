package io.hackle.sdk.core.evaluation.evaluator.experiment

import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.RemoteConfigRequest
import io.hackle.sdk.core.evaluation.flow.EvaluationFlow
import io.hackle.sdk.core.evaluation.flow.EvaluationFlowFactory
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.startsWith

@ExtendWith(MockKExtension::class)
internal class ExperimentEvaluatorTest {

    @MockK
    private lateinit var evaluationFlowFactory: EvaluationFlowFactory

    @InjectMockKs
    private lateinit var sut: ExperimentEvaluator

    @Test
    fun `supports`() {
        assertTrue(sut.supports(mockk<ExperimentRequest>()))
        assertFalse(sut.supports(mockk<RemoteConfigRequest<Any>>()))
    }

    @Nested
    inner class EvaluateTest {

        @Test
        fun `순환 호출`() {

            val request = experimentRequest()
            val context = Evaluators.context()
            context.add(request)

            val exception = assertThrows<IllegalArgumentException> { sut.evaluate(request, context) }

            expectThat(exception.message)
                .isNotNull()
                .startsWith("Circular evaluation has occurred")
        }

        @Test
        fun `evaluation - flow`() {
            // given
            val evaluation = mockk<ExperimentEvaluation>()
            val evaluationFlow = mockk<EvaluationFlow> {
                every { evaluate(any(), any()) } returns evaluation
            }
            every { evaluationFlowFactory.getFlow(any()) } returns evaluationFlow

            val request = experimentRequest()
            val context = Evaluators.context()

            // when
            val actual = sut.evaluate(request, context)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }
    }
}
