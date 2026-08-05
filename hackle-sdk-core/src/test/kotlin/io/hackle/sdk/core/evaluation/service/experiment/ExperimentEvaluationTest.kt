package io.hackle.sdk.core.evaluation.service.experiment

import io.hackle.sdk.core.support.Experiments
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isSameInstanceAs

internal class ExperimentEvaluationTest {

    @Test
    fun `entity 와 result 로 평가를 생성한다`() {
        val experiment = Experiments.config()
        val evaluateResult = Experiments.result()

        val evaluation = ExperimentEvaluation(entity = experiment, result = evaluateResult)

        expectThat(evaluation) {
            get { entity } isSameInstanceAs experiment
            get { result } isSameInstanceAs evaluateResult
        }
    }
}
