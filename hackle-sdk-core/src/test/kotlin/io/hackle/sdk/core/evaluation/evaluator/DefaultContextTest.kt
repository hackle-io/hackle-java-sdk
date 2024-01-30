package io.hackle.sdk.core.evaluation.evaluator

import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.RemoteConfigEvaluation
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.experiment
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.*
import javax.naming.event.EventContext

internal class DefaultContextTest {

    @Test
    fun `stack`() {

        val context = Evaluators.context()
        expectThat(context.stack).hasSize(0)

        val request1 = experimentRequest(experiment = experiment(id = 1))
        context.add(request1)
        val stack1 = context.stack
        expectThat(stack1).hasSize(1)

        val request2 = experimentRequest(experiment = experiment(id = 2))
        context.add(request2)
        val stack2 = context.stack
        expectThat(stack2).hasSize(2)

        context.remove(request2)
        expectThat(context.stack).hasSize(1)

        context.remove(request1)
        expectThat(context.stack).hasSize(0)

        expectThat(stack1).hasSize(1)
        expectThat(stack2).hasSize(2)
    }

    @Test
    fun `targetEvaluations`() {

        val context = Evaluators.context()
        expectThat(context.targetEvaluations).hasSize(0)

        val experiment = experiment(id = 1)

        val evaluation1 = mockk<RemoteConfigEvaluation<Any>>()
        context.add(evaluation1)
        val targetEvaluations1 = context.targetEvaluations
        expectThat(targetEvaluations1).hasSize(1)
        expectThat(context[experiment]).isNull()


        val evaluation2 = experimentEvaluation(experiment)
        context.add(evaluation2)
        val targetEvaluations2 = context.targetEvaluations
        expectThat(targetEvaluations1).hasSize(1)
        expectThat(targetEvaluations2).hasSize(2)
        expectThat(context[experiment]) isSameInstanceAs evaluation2

        expectThat(context[experiment(id = 2)]).isNull()
    }

    @Test
    fun `property`() {
        val context = Evaluators.context()
        val p1 = context.properties
        expectThat(p1).isEqualTo(mapOf())

        context.addProperty("a", 1)
        val p2 = context.properties
        expectThat(p1).isEqualTo(mapOf())
        expectThat(p2).isEqualTo(mapOf("a" to 1))
    }

    private fun experimentEvaluation(experiment: Experiment): ExperimentEvaluation {
        return mockk {
            every { this@mockk.experiment } returns experiment
        }
    }
}