package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.Variation.*
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.model.Experiment.Status.COMPLETED
import io.hackle.sdk.core.model.Experiment.Status.DRAFT
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.experiment
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.startsWith

internal class CompletedExperimentEvaluatorTest : FlowEvaluatorTest() {
    @Test
    fun `COMPLETED 상태면 위너 그룹 평가한다`() {
        // given
        val experiment = experiment(type = AB_TEST, status = COMPLETED) {
            variations {
                A(320)
                B(321)
                C(322)
            }
            winner(C)
        }

        val request = experimentRequest(experiment = experiment)
        val sut = CompletedExperimentEvaluator()

        // when
        val actual = sut.evaluate(request, context, nextFlow)

        // then
        expectThat(actual.reason) isEqualTo DecisionReason.EXPERIMENT_COMPLETED
        expectThat(actual.variationId) isEqualTo 322
    }

    @Test
    fun `COMPLETED 상태이지만 winner variation 이 없으면 예외 발생`() {
        // given
        val experiment = experiment(type = AB_TEST, status = COMPLETED) {
            variations {
                A(320)
                B(321)
                C(322)
            }
        }

        val request = experimentRequest(experiment = experiment)
        val sut = CompletedExperimentEvaluator()

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.evaluate(request, context, nextFlow)
        }

        // then
        expectThat(exception.message)
            .isNotNull()
            .startsWith("winner variation")
    }

    @Test
    fun `COMPLETED 상태가 아니면 다음 플로우를 실행한다`() {
        // given
        val experiment = experiment(type = AB_TEST, status = DRAFT) {
            variations(A, B)
        }
        val request = experimentRequest(experiment = experiment)
        val sut = CompletedExperimentEvaluator()

        // when
        val actual = sut.evaluate(request, context, nextFlow)

        // then
        expectThat(actual) isSameInstanceAs evaluation
    }
}