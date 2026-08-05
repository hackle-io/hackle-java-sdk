package io.hackle.sdk.core.evaluation.service.experiment.mode.local

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.event.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.flow.EvaluationFlow
import io.hackle.sdk.core.evaluation.service.experiment.flow.ExperimentLocalEvaluationFlowFactory
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.create
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
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
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.startsWith

@ExtendWith(MockKExtension::class)
internal class ExperimentLocalEvaluatorTest {

    @MockK
    private lateinit var evaluationFlowFactory: ExperimentLocalEvaluationFlowFactory

    @MockK
    private lateinit var eventRecorder: EvaluationEventRecorder

    @InjectMockKs
    private lateinit var sut: ExperimentLocalEvaluator

    @Test
    fun `supports - ExperimentLocalEvaluateRequest 만 지원한다`() {
        assertTrue(sut.supports(Experiments.localRequest()))
        assertFalse(sut.supports(Experiments.remoteRequest()))
    }

    @Nested
    inner class EvaluateTest {

        @Test
        fun `순환 호출이 발생하면 예외 발생`() {
            // given
            val request = Experiments.localRequest()
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
            val result = Experiments.result()
            every { evaluationFlowFactory.flow(any()) } returns EvaluationFlow.create(result)

            val request = Experiments.localRequest()

            // when
            val actual = sut.evaluate(request, Evaluators.context())

            // then
            expectThat(actual) {
                get { evaluation.entity } isSameInstanceAs request.entity
                get { evaluation.result } isSameInstanceAs result
            }
        }

        @Test
        fun `context 에 추가된 reference 평가를 response 에 포함한다`() {
            // given
            val result = Experiments.result()
            every { evaluationFlowFactory.flow(any()) } returns EvaluationFlow.create(result)

            val referenceEvaluation = Experiments.evaluation()
            val context = Evaluators.context()
            context.add(referenceEvaluation)

            // when
            val actual = sut.evaluate(Experiments.localRequest(), context)

            // then
            expectThat(actual) {
                get { references }.hasSize(1)
                get { references[0] } isSameInstanceAs referenceEvaluation
            }
        }

        @Test
        fun `flow 평가 결과가 없으면 TRAFFIC_NOT_ALLOCATED, Control Variation 으로 평가한다`() {
            // given
            every { evaluationFlowFactory.flow(any()) } returns EvaluationFlow.end()

            val request = Experiments.localRequest()

            // when
            val actual = sut.evaluate(request, Evaluators.context())

            // then
            expectThat(actual) {
                get { evaluation.result.reason } isEqualTo DecisionReason.TRAFFIC_NOT_ALLOCATED
                get { evaluation.result.variation.key } isEqualTo "A"
            }
        }
    }

    @Test
    fun `record - eventRecorder 로 기록한다`() {
        // given
        justRun { eventRecorder.record(any()) }
        val response = Experiments.response()

        // when
        sut.record(Experiments.localRequest(), response)

        // then
        verify(exactly = 1) {
            eventRecorder.record(response)
        }
    }
}
