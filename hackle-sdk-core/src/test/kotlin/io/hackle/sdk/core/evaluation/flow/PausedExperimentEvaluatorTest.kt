package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.Variation.A
import io.hackle.sdk.common.Variation.B
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.HackleUser
import io.hackle.sdk.core.model.experiment
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
        val experiment = experiment(type = Experiment.Type.AB_TEST, status = Experiment.Status.PAUSED) {
            variations {
                A(41)
                B(42)
            }
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
        val experiment = experiment(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.PAUSED) {
            variations {
                A(42)
                B(43)
            }
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
        val experiment = experiment(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.COMPLETED)
        val evaluation = mockk<Evaluation>()
        val nextFlow = mockk<EvaluationFlow> {
            every { evaluate(any(), any(), any(), any()) } returns evaluation
        }

        val sut = PausedExperimentEvaluator()

        // when
        val actual = sut.evaluate(mockk(), experiment, HackleUser.of("test_id"), "D", nextFlow)

        // then
        expectThat(actual) isSameInstanceAs evaluation
    }
}