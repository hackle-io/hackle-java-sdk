package io.hackle.sdk.core.model

import io.hackle.sdk.common.User
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
            val experiment = experiment(variations = listOf(1 to "A", 2 to "B"))

            expectThat(experiment.getVariationOrNull(1)) isEqualTo Variation(1, "A", false)
            expectThat(experiment.getVariationOrNull(2)) isEqualTo Variation(2, "B", false)
            expectThat(experiment.getVariationOrNull(3)).isNull()
        }

        @Test
        fun `get variation by key`() {
            val experiment = experiment(variations = listOf(1 to "A", 2 to "B"))

            expectThat(experiment.getVariationOrNull("A")) isEqualTo Variation(1, "A", false)
            expectThat(experiment.getVariationOrNull("B")) isEqualTo Variation(2, "B", false)
            expectThat(experiment.getVariationOrNull("C")).isNull()
        }
    }

    @Nested
    inner class OverriddenVariationTest {

        @Test
        fun `userId에 해당하는 수동할당이 없으면 null 리턴`() {
            val experiment = experiment(
                variations = listOf(1 to "A", 2 to "B"),
                overrides = emptyMap()
            )

            val variation = experiment.getOverriddenVariationOrNull(User.of("test"))
            expectThat(variation).isNull()
        }

        @Test
        fun `수동할당된 variationId에 해당하는 Variation이 없으면 예외 발생`() {
            // given
            val experiment = experiment(
                variations = listOf(1 to "A", 2 to "B"),
                overrides = mapOf("test_id" to 3)
            )

            // when
            val exception = assertThrows<IllegalArgumentException> {
                experiment.getOverriddenVariationOrNull(User.of("test_id"))
            }

            // then
            expectThat(exception.message)
                .isNotNull()
                .isEqualTo("experiment[42] variation[3]")
        }

        @Test
        fun `수동할당된 Variation을 갸져온다`() {
            // given
            val experiment = experiment(
                variations = listOf(1 to "A", 2 to "B"),
                overrides = mapOf("test_id" to 2)
            )

            // when
            val variation = experiment.getOverriddenVariationOrNull(User.of("test_id"))

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
            val experiment = Experiment.Completed(
                id = 42,
                key = 320,
                type = Experiment.Type.AB_TEST,
                variations = listOf(
                    Variation(1, "A", false),
                    Variation(2, "B", false),
                ),
                overrides = emptyMap(),
                winnerVariationId = 2
            )

            expectThat(experiment.winnerVariation) isEqualTo Variation(2, "B", false)
        }

        @Test
        fun `get winner variation fail`() {
            val experiment = Experiment.Completed(
                id = 42,
                key = 320,
                type = Experiment.Type.AB_TEST,
                variations = listOf(
                    Variation(1, "A", false),
                    Variation(2, "B", false),
                ),
                overrides = emptyMap(),
                winnerVariationId = 3
            )

            assertThrows<IllegalArgumentException> { experiment.winnerVariation }
        }
    }

    private fun experiment(variations: List<Pair<Int, String>>, overrides: Map<String, Long> = emptyMap()): Experiment {

        return Experiment.Draft(
            id = 42,
            key = 320,
            type = Experiment.Type.AB_TEST,
            variations = variations.map { Variation(it.first.toLong(), it.second, false) },
            overrides = overrides
        )

    }
}