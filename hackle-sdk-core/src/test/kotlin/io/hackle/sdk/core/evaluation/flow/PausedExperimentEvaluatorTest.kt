package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Variation
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

internal class PausedExperimentEvaluatorTest {


    @Test
    fun `AB 테스트가 PAUSED 상태면 기본그룹, EXPERIMENT_PAUSED으로 평가한다`() {
        // given
        val experiment = mockk<Experiment.Paused> {
            every { type } returns Experiment.Type.AB_TEST
            every { getVariationOrNull(any<String>()) } returns Variation(42, "B", false)
        }

        val sut = PausedExperimentEvaluator()

        // when
        val actual = sut.evaluate(mockk(), experiment, mockk(), "B", mockk())

        // then
        expectThat(actual) isEqualTo Evaluation(42, "B", DecisionReason.EXPERIMENT_PAUSED)
    }

    @Test
    fun `기능 플래그가 PAUSED 상태면 기본그룹, FEATURE_FLAG_INACTIVE 로 평가한다`() {
        // given
        val experiment = mockk<Experiment.Paused> {
            every { type } returns Experiment.Type.FEATURE_FLAG
            every { getVariationOrNull(any<String>()) } returns Variation(42, "A", false)
        }

        val sut = PausedExperimentEvaluator()

        // when
        val actual = sut.evaluate(mockk(), experiment, mockk(), "A", mockk())

        // then
        expectThat(actual) isEqualTo Evaluation(42, "A", DecisionReason.FEATURE_FLAG_INACTIVE)
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