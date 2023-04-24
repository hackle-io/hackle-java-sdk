package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.Variation.A
import io.hackle.sdk.common.Variation.B
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.experiment
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

internal class PausedExperimentEvaluatorTest : FlowEvaluatorTest() {


    @Test
    fun `AB 테스트가 PAUSED 상태면 기본그룹, EXPERIMENT_PAUSED으로 평가한다`() {
        // given
        val experiment = experiment(type = Experiment.Type.AB_TEST, status = Experiment.Status.PAUSED) {
            variations {
                A(41)
                B(42)
            }
        }
        val request = experimentRequest(experiment = experiment)
        val sut = PausedExperimentEvaluator()

        // when
        val actual = sut.evaluate(request, context, nextFlow)

        // then
        expectThat(actual.reason) isEqualTo DecisionReason.EXPERIMENT_PAUSED
        expectThat(actual.variationId) isEqualTo 41
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
        val request = experimentRequest(experiment = experiment)
        val sut = PausedExperimentEvaluator()

        // when
        val actual = sut.evaluate(request, context, nextFlow)

        // then
        expectThat(actual.reason) isEqualTo DecisionReason.FEATURE_FLAG_INACTIVE
        expectThat(actual.variationId) isEqualTo 42
    }

    @Test
    fun `PAUSED 상태가 아니면 다음 플로우를 실행한다`() {
        // given
        val experiment = experiment(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.COMPLETED)
        val request = experimentRequest(experiment = experiment)
        val sut = PausedExperimentEvaluator()

        // when
        val actual = sut.evaluate(request, context, nextFlow)

        // then
        expectThat(actual) isSameInstanceAs evaluation
    }
}