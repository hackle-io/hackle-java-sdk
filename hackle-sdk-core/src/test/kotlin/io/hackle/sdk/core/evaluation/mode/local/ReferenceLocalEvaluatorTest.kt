package io.hackle.sdk.core.evaluation.mode.local

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluation
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.workspace.config.entity.ExperimentConfig
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

class ReferenceLocalEvaluatorTest {

    @Test
    fun `이미 평가된 reference 면 캐시된 평가를 반환하고 다시 평가하지 않는다`() {
        // given
        val experiment = Experiments.config(id = 2)
        val cachedEvaluation = Experiments.evaluation(entity = experiment)
        val context = Evaluators.context()
        context.add(cachedEvaluation)

        val sut = TestReferenceLocalEvaluator(Experiments.evaluation(entity = experiment))

        // when
        val actual = sut.evaluate(Experiments.localRequest(), context, experiment)

        // then
        expectThat(actual) isSameInstanceAs cachedEvaluation
        expectThat(sut.evaluateCount) isEqualTo 0
    }

    @Test
    fun `평가되지 않은 reference 면 평가하고 context 에 추가한다`() {
        // given
        val experiment = Experiments.config(id = 2)
        val evaluation = Experiments.evaluation(entity = experiment)
        val context = Evaluators.context()

        val sut = TestReferenceLocalEvaluator(evaluation)

        // when
        val actual = sut.evaluate(Experiments.localRequest(), context, experiment)

        // then
        expectThat(actual) isSameInstanceAs evaluation
        expectThat(sut.evaluateCount) isEqualTo 1
        expectThat(context[experiment]) isSameInstanceAs evaluation
    }

    private class TestReferenceLocalEvaluator(
        private val evaluation: ExperimentEvaluation,
    ) : ReferenceLocalEvaluator<ExperimentConfig, ExperimentEvaluation>() {

        var evaluateCount = 0

        override fun doEvaluate(
            parentRequest: LocalEvaluateRequest,
            context: Evaluator.Context,
            reference: ExperimentConfig,
        ): ExperimentEvaluation {
            evaluateCount++
            return evaluation
        }
    }
}
