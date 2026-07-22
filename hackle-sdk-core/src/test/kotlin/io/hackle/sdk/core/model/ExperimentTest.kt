package io.hackle.sdk.core.model

import io.hackle.sdk.core.model.Experiment.Status.COMPLETED
import io.hackle.sdk.core.model.Experiment.Status.DRAFT
import io.hackle.sdk.core.model.Experiment.Status.PAUSED
import io.hackle.sdk.core.model.Experiment.Status.RUNNING
import io.hackle.sdk.core.support.Experiments
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.startsWith

internal class ExperimentTest {

    @Nested
    inner class VariationTest {

        @Test
        fun `get variation by id`() {
            val experiment = Experiments.config(
                variations = listOf(
                    Experiments.variation(id = 1, key = "A"),
                    Experiments.variation(id = 2, key = "B"),
                )
            )

            expectThat(experiment.getVariationOrNull(1)) isEqualTo Variation(1, "A", false, null)
            expectThat(experiment.getVariationOrNull(2)) isEqualTo Variation(2, "B", false, null)
            expectThat(experiment.getVariationOrNull(3)).isNull()
        }

        @Test
        fun `get variation by key`() {
            val experiment = Experiments.config(
                variations = listOf(
                    Experiments.variation(id = 1, key = "A"),
                    Experiments.variation(id = 2, key = "B"),
                )
            )

            expectThat(experiment.getVariationOrNull("A")) isEqualTo Variation(1, "A", false, null)
            expectThat(experiment.getVariationOrNull("B")) isEqualTo Variation(2, "B", false, null)
            expectThat(experiment.getVariationOrNull("C")).isNull()
        }
    }

    @Nested
    inner class ControlVariationTest {

        @Test
        fun `key 가 A 인 variation 을 리턴한다`() {
            val experiment = Experiments.config(
                variations = listOf(
                    Experiments.variation(id = 1, key = "A"),
                    Experiments.variation(id = 2, key = "B"),
                )
            )

            expectThat(experiment.controlVariation) isEqualTo Variation(1, "A", false, null)
        }

        @Test
        fun `key 가 A 인 variation 이 없으면 예외 발생`() {
            val experiment = Experiments.config(
                id = 42,
                variations = listOf(
                    Experiments.variation(id = 2, key = "B"),
                )
            )

            val exception = assertThrows<IllegalArgumentException> {
                experiment.controlVariation
            }

            expectThat(exception.message)
                .isNotNull()
                .startsWith("ControlVariation")
        }
    }

    @Nested
    inner class CompletedExperimentTest {

        @Test
        fun `get winner variation`() {
            val experiment = Experiments.config(
                id = 42,
                status = COMPLETED,
                variations = listOf(
                    Experiments.variation(id = 41, key = "A"),
                    Experiments.variation(id = 42, key = "B"),
                ),
                winnerVariationKey = "B"
            )

            expectThat(experiment.winnerVariation) isEqualTo Variation(42, "B", false, null)
        }

        @Test
        fun `get winner variation fail`() {
            val experiment = Experiments.config(
                id = 42,
                status = COMPLETED,
                variations = listOf(
                    Experiments.variation(id = 41, key = "A"),
                    Experiments.variation(id = 42, key = "B"),
                )
            )

            expectThat(experiment.winnerVariation).isNull()
        }
    }

    @Test
    fun `status`() {
        expectThat(Experiment.Status.from("READY")) isEqualTo DRAFT
        expectThat(Experiment.Status.from("RUNNING")) isEqualTo RUNNING
        expectThat(Experiment.Status.from("PAUSED")) isEqualTo PAUSED
        expectThat(Experiment.Status.from("STOPPED")) isEqualTo COMPLETED
        expectThat(Experiment.Status.from("UNKNOWN")).isNull()
    }

    @Test
    fun `equalsAndHashCode`() {
        val e1 = Experiments.config(id = 1)
        val e11 = Experiments.config(id = 1)
        val e2 = Experiments.config(id = 2)

        assertTrue(e1 == e1)
        assertTrue(e1 == e11)
        assertTrue(e1 != e2)
        assertTrue(!e1.equals("e1"))

        assertTrue(e1.hashCode() == e1.hashCode())
        assertTrue(e1.hashCode() == e11.hashCode())
        assertTrue(e1.hashCode() != e2.hashCode())
    }
}
