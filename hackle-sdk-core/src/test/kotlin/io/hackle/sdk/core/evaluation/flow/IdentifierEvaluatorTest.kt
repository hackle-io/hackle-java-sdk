package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.Variation.A
import io.hackle.sdk.common.Variation.B
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.Experiment.Status.RUNNING
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.experiment
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

internal class IdentifierEvaluatorTest {

    private val sut = IdentifierEvaluator()

    @Test
    fun `identifierType 에 대한 식별자가 있으면 다음 플로우 실행`() {
        // given
        val user = HackleUser.of("test_id")
        val experiment = experiment(type = AB_TEST, status = RUNNING) {
            variations {
                A(42)
                B(43)
            }
        }
        val workspace = mockk<Workspace>()
        val nextFlow = mockk<EvaluationFlow>()
        val evaluation = mockk<Evaluation>()
        every { nextFlow.evaluate(any(), any(), any(), any()) } returns evaluation

        // when
        val actual = sut.evaluate(workspace, experiment, user, "A", nextFlow)

        // then
        expectThat(actual) isSameInstanceAs evaluation
        verify(exactly = 1) {
            nextFlow.evaluate(workspace, experiment, user, "A")
        }
    }

    @Test
    fun `identifierType 에 대한 식별자가 없으면 IDENTIFIER_NOT_FOUND`() {
        // given
        val user = HackleUser.of("test_id")
        val experiment = experiment(type = AB_TEST, status = RUNNING, identifierType = "hello") {
            variations {
                A(42)
                B(43)
            }
        }
        val workspace = mockk<Workspace>()
        val nextFlow = mockk<EvaluationFlow>()
        val evaluation = mockk<Evaluation>()

        // when
        val actual = sut.evaluate(workspace, experiment, user, "A", nextFlow)

        // then
        expectThat(actual) isEqualTo Evaluation(42, "A", DecisionReason.IDENTIFIER_NOT_FOUND, null)
    }
}
