package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.Experiment
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

internal class PausedExperimentEvaluatorTest {


    @Test
    fun `PAUSED 상태면 기본그룹으로 평가한다`() {
        // given
        val experiment = mockk<Experiment.Paused>()

        val sut = PausedExperimentEvaluator()

        // when
        val actual = sut.evaluate(mockk(), experiment, mockk(), "B", mockk())

        // then
        expectThat(actual) isEqualTo Evaluation(null, "B", DecisionReason.EXPERIMENT_PAUSED)
    }

    @Test
    fun `PAUSED 상태가 아니면 다음 플로우를 실행한다`() {
        // given
        val experiment = mockk<Experiment>()
        val evaluation = mockk<Evaluation>()
        val nextFlow = mockk<EvaluationFlow> {
            every { evaluate(any(), any(), any(), any()) } returns evaluation
        }

        val sut = PausedExperimentEvaluator()

        // when
        val actual = sut.evaluate(mockk(), experiment, User.of("test_id"), "D", nextFlow)

        // then
        expectThat(actual) isSameInstanceAs evaluation
    }
}