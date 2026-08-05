package io.hackle.sdk.core.evaluation.service.experiment

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.support.Experiments
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.startsWith

internal class ExperimentEvaluateResultTest {

    @Test
    fun `of - reason 과 variation 으로 생성한다`() {
        val variation = Experiments.variation(key = "B")

        val result = ExperimentEvaluateResult.of(DecisionReason.TRAFFIC_ALLOCATED, variation)

        expectThat(result) {
            get { reason } isEqualTo DecisionReason.TRAFFIC_ALLOCATED
            get { this.variation } isSameInstanceAs variation
        }
    }

    @Test
    fun `ofControl - Control Variation 으로 생성한다`() {
        val experiment = Experiments.config(
            variations = listOf(
                Experiments.variation(id = 320, key = "A"),
                Experiments.variation(id = 321, key = "B"),
            )
        )
        val request = Experiments.localRequest(experiment = experiment)

        val result = ExperimentEvaluateResult.ofControl(DecisionReason.TRAFFIC_NOT_ALLOCATED, request)

        expectThat(result) {
            get { reason } isEqualTo DecisionReason.TRAFFIC_NOT_ALLOCATED
            get { variation.id } isEqualTo 320L
            get { variation.key } isEqualTo "A"
        }
    }

    @Test
    fun `ofControl - Control Variation 이 없으면 예외 발생`() {
        val experiment = Experiments.config(
            variations = listOf(
                Experiments.variation(id = 321, key = "B"),
            )
        )
        val request = Experiments.localRequest(experiment = experiment)

        val exception = assertThrows<IllegalArgumentException> {
            ExperimentEvaluateResult.ofControl(DecisionReason.TRAFFIC_NOT_ALLOCATED, request)
        }

        expectThat(exception.message)
            .isNotNull()
            .startsWith("ControlVariation")
    }

    @Test
    fun `with - reason 만 변경하고 variation 은 유지한다`() {
        val variation = Experiments.variation(key = "B")
        val result = ExperimentEvaluateResult.of(DecisionReason.TRAFFIC_ALLOCATED, variation)

        val actual = result.with(DecisionReason.OVERRIDDEN)

        expectThat(actual) {
            get { reason } isEqualTo DecisionReason.OVERRIDDEN
            get { this.variation } isSameInstanceAs variation
        }
    }
}
