package io.hackle.sdk.core.model

import io.hackle.sdk.common.Variation.A
import io.hackle.sdk.common.Variation.B
import io.hackle.sdk.core.model.Experiment.Status.*
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
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

            expectThat(experiment.getVariationOrNull(1)) isEqualTo Variation(1, "A", false)
            expectThat(experiment.getVariationOrNull(2)) isEqualTo Variation(2, "B", false)
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

            expectThat(experiment.getVariationOrNull("A")) isEqualTo Variation(1, "A", false)
            expectThat(experiment.getVariationOrNull("B")) isEqualTo Variation(2, "B", false)
            expectThat(experiment.getVariationOrNull("C")).isNull()
        }
    }

    @Nested
    inner class OverriddenVariationTest {

        @Test
        fun `userId에 해당하는 수동할당이 없으면 null 리턴`() {
            val experiment = experiment(type = AB_TEST, status = DRAFT) {
                variations {
                    A(1, false)
                    B(2, false)
                }
            }

            val variation = experiment.getOverriddenVariationOrNull(HackleUser.of("test"))
            expectThat(variation).isNull()
        }

        @Test
        fun `수동할당된 variationId에 해당하는 Variation이 없으면 예외 발생`() {
            // given
            val experiment = experiment(id = 42, type = AB_TEST, status = DRAFT) {
                variations {
                    A(1, false)
                    B(2, false)
                }
            }.copy(overrides = mapOf("test_id" to 3))


            // when
            val exception = assertThrows<IllegalArgumentException> {
                experiment.getOverriddenVariationOrNull(HackleUser.of("test_id"))
            }

            // then
            expectThat(exception.message)
                .isNotNull()
                .isEqualTo("experiment[42] variation[3]")
        }

        @Test
        fun `수동할당된 Variation을 갸져온다`() {
            // given
            val experiment = experiment(id = 42, type = AB_TEST, status = DRAFT) {
                variations {
                    A(1, false)
                    B(2, false)
                }
                overrides {
                    B("test_id")
                }
            }

            // when
            val variation = experiment.getOverriddenVariationOrNull(HackleUser.of("test_id"))

            // then
            expectThat(variation)
                .isNotNull()
                .isEqualTo(Variation(2, "B", false))
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

            expectThat(experiment.winnerVariation) isEqualTo Variation(42, "B", false)
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
}
