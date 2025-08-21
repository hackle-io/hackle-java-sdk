package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentFlow
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.eligibility.InAppMessageEligibilityFlow
import io.hackle.sdk.core.model.InAppMessages
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

internal class EvaluationFlowTest {

    @Nested
    inner class EvaluateTest {

        @Test
        fun `when end of flow then returns null`() {
            val flow: ExperimentFlow = EvaluationFlow.end()
            val actual = flow.evaluate(experimentRequest(), Evaluators.context())
            expectThat(actual).isNull()
        }

        @Test
        fun `when flow meed decision then evaluate flow`() {
            val evaluation = InAppMessages.evaluation()
            val flow: InAppMessageEligibilityFlow = InAppMessageEligibilityFlow.create(evaluation)
            val actual = flow.evaluate(InAppMessages.eligibilityRequest(), Evaluators.context())
            expectThat(actual) isSameInstanceAs evaluation
        }
    }

    @Test
    fun `of`() {

        val f1 = mockk<FlowEvaluator<Evaluator.Request, Evaluator.Evaluation>>()
        val f2 = mockk<FlowEvaluator<Evaluator.Request, Evaluator.Evaluation>>()
        val f3 = mockk<FlowEvaluator<Evaluator.Request, Evaluator.Evaluation>>()

        val flow = EvaluationFlow.of(f1, f2, f3)

        expectThat(flow)
            .isDecisionWith(f1)
            .isDecisionWith(f2)
            .isDecisionWith(f3)
            .isEnd()
    }
}
