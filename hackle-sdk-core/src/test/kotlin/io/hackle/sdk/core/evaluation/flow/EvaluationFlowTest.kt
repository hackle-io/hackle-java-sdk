package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Variation
import io.hackle.sdk.core.workspace.Workspace
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
            val experiment = mockk<Experiment> {
                every { getVariationOrNull(any<String>()) } returns Variation(42, "F", false)
            }
            val evaluation = EvaluationFlow.End.evaluate(mockk(), experiment, mockk(), "F")
            expectThat(evaluation) isEqualTo Evaluation(42, "F", DecisionReason.TRAFFIC_NOT_ALLOCATED)
        }

        @Test
        fun `Decision인 경우 flowEvaluator를 호출한다`() {
            // given
            val workspace = mockk<Workspace>()
            val experiment = mockk<Experiment>()
            val user = mockk<User>()

            val evaluation = mockk<Evaluation>()
            val nextFlow = mockk<EvaluationFlow>()
            val flowEvaluator = mockk<FlowEvaluator> {
                every { evaluate(any(), any(), any(), any(), any()) } returns evaluation
            }

            val sut = EvaluationFlow.Decision(flowEvaluator, nextFlow)

            // when
            val actual = sut.evaluate(workspace, experiment, user, "C")

            // then
            expectThat(actual) isSameInstanceAs evaluation
            verify {
                flowEvaluator.evaluate(workspace, experiment, user, "C", nextFlow)
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
