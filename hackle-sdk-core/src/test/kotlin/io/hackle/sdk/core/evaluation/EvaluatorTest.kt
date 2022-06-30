package io.hackle.sdk.core.evaluation

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.flow.EvaluationFlow
import io.hackle.sdk.core.evaluation.flow.EvaluationFlowFactory
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo


@ExtendWith(MockKExtension::class)
internal class EvaluatorTest {

    @MockK
    private lateinit var evaluationFlowFactory: EvaluationFlowFactory

    @InjectMockKs
    private lateinit var sut: Evaluator

    @Test
    fun `evaluationFlowFactory에서 ExperimentType으로 Flow를 가져와서 평가한다`() {
        // given

        val evaluation = Evaluation(430, "B", DecisionReason.TRAFFIC_ALLOCATED)

        val evaluationFlow = mockk<EvaluationFlow> {
            every { evaluate(any(), any(), any(), any()) } returns evaluation
        }

        every { evaluationFlowFactory.getFlow(any()) } returns evaluationFlow

        val workspace = mockk<Workspace>()
        val experiment = mockk<Experiment> { every { type } returns Experiment.Type.AB_TEST }
        val user = HackleUser.of("test_id")

        // when
        val actual = sut.evaluate(workspace, experiment, user, "J")

        // then
        expectThat(actual) isEqualTo evaluation
        verify(exactly = 1) {
            evaluationFlow.evaluate(workspace, experiment, user, "J")
        }
    }

}