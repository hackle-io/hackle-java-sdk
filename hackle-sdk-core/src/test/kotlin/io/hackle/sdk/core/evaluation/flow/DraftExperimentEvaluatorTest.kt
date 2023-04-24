package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.Variation.A
import io.hackle.sdk.common.Variation.B
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.model.Experiment.Status.DRAFT
import io.hackle.sdk.core.model.Experiment.Status.RUNNING
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.experiment
import io.mockk.verify
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

internal class DraftExperimentEvaluatorTest : FlowEvaluatorTest() {

    @Test
    fun `DRAFT상태면 기본그룹으로 평가한다`() {
        // given
        val experiment = experiment(type = AB_TEST, status = DRAFT) {
            variations {
                A(42)
                B(43)
            }
        }
        val request = experimentRequest(experiment = experiment)
        val sut = DraftExperimentEvaluator()

        // when
        val actual = sut.evaluate(request, Evaluators.context(), nextFlow)

        // then
        expectThat(actual.reason) isEqualTo DecisionReason.EXPERIMENT_DRAFT
        expectThat(actual.variationId) isEqualTo 42
    }

    @Test
    fun `DRAFT상태가 아니면 다음Flow로 평가한다`() {
        // given
        val experiment = experiment(type = AB_TEST, status = RUNNING)
        val request = experimentRequest(experiment = experiment)
        val sut = DraftExperimentEvaluator()

        // when
        val actual = sut.evaluate(request, Evaluators.context(), nextFlow)

        // then
        expectThat(actual) isSameInstanceAs evaluation
        verify(exactly = 1) {
            nextFlow.evaluate(any(), any())
        }
    }
}