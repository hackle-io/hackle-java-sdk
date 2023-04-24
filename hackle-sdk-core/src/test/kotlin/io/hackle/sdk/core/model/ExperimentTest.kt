package io.hackle.sdk.core.model

import io.hackle.sdk.common.Variation.A
import io.hackle.sdk.common.Variation.B
import io.hackle.sdk.core.model.Experiment.Status.*
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

internal class ExperimentTest {

    @Nested
    inner class VariationTest {

        @Test
        fun `get variation by id`() {
            val experiment = experiment(type = AB_TEST, status = DRAFT) {
                variations {
                    A(1, false)
                    B(2, false)
                }
            }

            expectThat(experiment.getVariationOrNull(1)) isEqualTo Variation(1, "A", false, null)
            expectThat(experiment.getVariationOrNull(2)) isEqualTo Variation(2, "B", false, null)
            expectThat(experiment.getVariationOrNull(3)).isNull()
        }

        @Test
        fun `get variation by key`() {
            val experiment = experiment(type = AB_TEST, status = DRAFT) {
                variations {
                    A(1, false)
                    B(2, false)
                }
            }

            expectThat(experiment.getVariationOrNull("A")) isEqualTo Variation(1, "A", false, null)
            expectThat(experiment.getVariationOrNull("B")) isEqualTo Variation(2, "B", false, null)
            expectThat(experiment.getVariationOrNull("C")).isNull()
        }
    }

    @Nested
    inner class CompletedExperimentTest {

        @Test
        fun `get winner variation`() {

            val experiment = experiment(id = 42, type = AB_TEST, status = COMPLETED) {
                variations {
                    A(41)
                    B(42)
                }
                winner(B)
            }

            expectThat(experiment.winnerVariation) isEqualTo Variation(42, "B", false, null)
        }

        @Test
        fun `get winner variation fail`() {
            val experiment = experiment(id = 42, type = AB_TEST, status = COMPLETED) {
                variations {
                    A(41)
                    B(42)
                }
            }

            expectThat(experiment.winnerVariation).isNull()
        }
    }

    @Test
    fun `status`() {
        expectThat(Experiment.Status.fromExecutionStatusOrNull("READY")) isEqualTo DRAFT
        expectThat(Experiment.Status.fromExecutionStatusOrNull("RUNNING")) isEqualTo RUNNING
        expectThat(Experiment.Status.fromExecutionStatusOrNull("PAUSED")) isEqualTo PAUSED
        expectThat(Experiment.Status.fromExecutionStatusOrNull("STOPPED")) isEqualTo COMPLETED
    }

    @Test
    fun `equalsAndHashCode`() {
        val e1 = experiment(1)
        val e11 = experiment(1)
        val e2 = experiment(2)

        assertTrue(e1 == e1)
        assertTrue(e1 == e11)
        assertTrue(e1 != e2)
        assertTrue(!e1.equals("e1"))

        assertTrue(e1.hashCode() == e1.hashCode())
        assertTrue(e1.hashCode() == e11.hashCode())
        assertTrue(e1.hashCode() != e2.hashCode())
    }
}
