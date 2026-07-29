package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.EvaluateResult
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.service.experiment.flow.ExperimentLocalEvaluationFlow
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow.InAppMessageEligibilityLocalEvaluationFlow
import io.hackle.sdk.core.support.*
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
        fun `end`() {
            val flow: ExperimentLocalEvaluationFlow = EvaluationFlow.end()
            val actual = flow.evaluate(Experiments.localRequest(), Evaluators.context())
            expectThat(actual).isNull()
        }

        @Test
        fun `step`() {
            val result = InAppMessages.eligibilityResult()
            val flow: InAppMessageEligibilityLocalEvaluationFlow = EvaluationFlow.create(result)
            val actual = flow.evaluate(InAppMessages.eligibilityLocalRequest(), Evaluators.context())
            expectThat(actual) isSameInstanceAs result
        }
    }

    @Test
    fun `of`() {

        val f1 = mockk<FlowEvaluator<EvaluateRequest, EvaluateResult>>()
        val f2 = mockk<FlowEvaluator<EvaluateRequest, EvaluateResult>>()
        val f3 = mockk<FlowEvaluator<EvaluateRequest, EvaluateResult>>()

        val flow = EvaluationFlow.of(f1, f2, f3)

        expectThat(flow)
            .isStepWith(f1)
            .isStepWith(f2)
            .isStepWith(f3)
            .isEnd()
    }


    @Test
    fun `plus`() {
        val fe1 = mockk<FlowEvaluator<EvaluateRequest, EvaluateResult>>()
        val fe2 = mockk<FlowEvaluator<EvaluateRequest, EvaluateResult>>()
        val fe3 = mockk<FlowEvaluator<EvaluateRequest, EvaluateResult>>()
        val fe4 = mockk<FlowEvaluator<EvaluateRequest, EvaluateResult>>()

        val f1 = EvaluationFlow.of(fe1, fe2)
        val f2 = EvaluationFlow.of(fe3, fe4)

        val f = f1 + f2

        expectThat(f)
            .isStepWith(fe1)
            .isStepWith(fe2)
            .isStepWith(fe3)
            .isStepWith(fe4)
            .isEnd()
    }

    @Test
    fun `concat`() {
        val fe1 = mockk<FlowEvaluator<EvaluateRequest, EvaluateResult>>()
        val fe2 = mockk<FlowEvaluator<EvaluateRequest, EvaluateResult>>()
        val fe3 = mockk<FlowEvaluator<EvaluateRequest, EvaluateResult>>()

        val flow = EvaluationFlow.concat(
            EvaluationFlow.of(fe1),
            EvaluationFlow.end(),
            EvaluationFlow.of(fe2, fe3),
        )

        expectThat(flow)
            .isStepWith(fe1)
            .isStepWith(fe2)
            .isStepWith(fe3)
            .isEnd()
    }

    @Test
    fun `concat - empty`() {
        val flow = EvaluationFlow.concat<EvaluateRequest, EvaluateResult>()
        expectThat(flow).isEnd()
    }
}
