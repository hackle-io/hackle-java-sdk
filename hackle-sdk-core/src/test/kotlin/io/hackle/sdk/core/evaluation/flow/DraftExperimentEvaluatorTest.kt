package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.Variation.A
import io.hackle.sdk.common.Variation.B
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.Experiment.Status.DRAFT
import io.hackle.sdk.core.model.Experiment.Status.RUNNING
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.experiment
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

internal class DraftExperimentEvaluatorTest {

    @Test
    fun `DRAFT상태면 기본그룹으로 평가한다`() {
        // given
        val experiment = experiment(type = AB_TEST, status = DRAFT) {
            variations {
                A(42)
                B(43)
            }
        }
        val sut = DraftExperimentEvaluator()

        // when
        val actual = sut.evaluate(mockk(), experiment, mockk(), "B", mockk())

        // then
        expectThat(actual) isEqualTo Evaluation(43, "B", DecisionReason.EXPERIMENT_DRAFT)
    }

    @Test
    fun `DRAFT상태가 아니면 다음Flow로 평가한다`() {
        // given
        val experiment = experiment(type = AB_TEST, status = RUNNING)
        val evaluation = mockk<Evaluation>()
        val nextFlow = mockk<EvaluationFlow> {
            every { evaluate(any(), any(), any(), any()) } returns evaluation
        }

        val sut = DraftExperimentEvaluator()

        // when
        val actual = sut.evaluate(mockk(), experiment, mockk(), "D", nextFlow)

        // then
        expectThat(actual) isSameInstanceAs evaluation
        verify(exactly = 1) {
            nextFlow.evaluate(any(), experiment, any(), any())
        }
    }
}