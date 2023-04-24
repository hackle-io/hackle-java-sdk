package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

internal class EvaluationFlowTest {

    @Nested
    inner class EvaluateTest {
        @Test
        fun `End는 TRAFFIC_NOT_ALLOCATED + 기본그룹으로 평가됨`() {
            val request = experimentRequest()
            val evaluation = EvaluationFlow.End.evaluate(request, Evaluators.context())
            expectThat(evaluation.reason) isEqualTo DecisionReason.TRAFFIC_NOT_ALLOCATED
            expectThat(evaluation.variationKey) isEqualTo "A"
        }

        @Test
        fun `Decision인 경우 flowEvaluator를 호출한다`() {
            // given
            val evaluation = mockk<ExperimentEvaluation>()
            val nextFlow = mockk<EvaluationFlow>()
            val flowEvaluator = mockk<FlowEvaluator> {
                every { evaluate(any(), any(), any()) } returns evaluation
            }
            val request = experimentRequest()

            val sut = EvaluationFlow.Decision(flowEvaluator, nextFlow)

            // when
            val actual = sut.evaluate(request, Evaluators.context())

            // then
            expectThat(actual) isSameInstanceAs evaluation
            verify {
                flowEvaluator.evaluate(request, any(), nextFlow)
            }
        }

    }

    @Test
    fun `of`() {
        val flowEvaluator1 = mockk<FlowEvaluator>()
        val flowEvaluator2 = mockk<FlowEvaluator>()
        val flowEvaluator3 = mockk<FlowEvaluator>()

        val evaluationFlow = EvaluationFlow.of(flowEvaluator1, flowEvaluator2, flowEvaluator3)

        expectThat(evaluationFlow)
            .isA<EvaluationFlow.Decision>()
            .and { get { flowEvaluator } isSameInstanceAs flowEvaluator1 }
            .get { nextFlow }
            .isA<EvaluationFlow.Decision>()
            .and { get { flowEvaluator } isSameInstanceAs flowEvaluator2 }
            .get { nextFlow }
            .isA<EvaluationFlow.Decision>()
            .and { get { flowEvaluator } isSameInstanceAs flowEvaluator3 }
            .get { nextFlow }
            .isA<EvaluationFlow.End>()
    }
}
