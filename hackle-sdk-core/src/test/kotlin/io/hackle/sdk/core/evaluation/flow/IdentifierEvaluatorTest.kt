package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.Variation.A
import io.hackle.sdk.common.Variation.B
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.model.Experiment.Status.RUNNING
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.experiment
import io.mockk.verify
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

internal class IdentifierEvaluatorTest : FlowEvaluatorTest() {

    private val sut = IdentifierEvaluator()

    @Test
    fun `identifierType 에 대한 식별자가 있으면 다음 플로우 실행`() {
        // given
        val experiment = experiment(type = AB_TEST, status = RUNNING) {
            variations {
                A(42)
                B(43)
            }
        }
        val request = experimentRequest(experiment = experiment)

        // when
        val actual = sut.evaluate(request, context, nextFlow)

        // then
        expectThat(actual) isSameInstanceAs evaluation
        verify(exactly = 1) {
            nextFlow.evaluate(any(), any())
        }
    }

    @Test
    fun `identifierType 에 대한 식별자가 없으면 IDENTIFIER_NOT_FOUND`() {
        // given
        val experiment = experiment(type = AB_TEST, status = RUNNING, identifierType = "hello") {
            variations {
                A(42)
                B(43)
            }
        }
        val request = experimentRequest(experiment = experiment)

        // when
        val actual = sut.evaluate(request, context, nextFlow)

        // then
        expectThat(actual.reason) isEqualTo DecisionReason.IDENTIFIER_NOT_FOUND
        expectThat(actual.variationKey) isEqualTo "A"
    }
}
