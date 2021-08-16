package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Variation
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

internal class OverrideEvaluatorTest {

    @Test
    fun `override된 사용자인 경우 overriddenVariation으로 평가한다`() {
        // given
        val user = User.of("test_id")
        val variation = Variation(320, "B", false)
        val experiment = mockk<Experiment> {
            every { getOverriddenVariationOrNull(user) } returns variation
        }

        val sut = OverrideEvaluator()

        // when
        val actual = sut.evaluate(mockk(), experiment, user, "C", mockk())

        // then
        expectThat(actual) isEqualTo Evaluation(null, "B", DecisionReason.OVERRIDDEN)
    }

    @Test
    fun `override된 사용자가 아닌경우 다음 Flow로 평가한다`() {
        // given
        val user = User.of("test_id")
        val experiment = mockk<Experiment> {
            every { getOverriddenVariationOrNull(user) } returns null
        }

        val evaluation = mockk<Evaluation>()
        val nextFlow = mockk<EvaluationFlow> {
            every { evaluate(any(), experiment, user, any()) } returns evaluation
        }

        val sut = OverrideEvaluator()

        // when
        val actual = sut.evaluate(mockk(), experiment, user, "C", nextFlow)

        // then
        expectThat(actual) isSameInstanceAs evaluation
        verify(exactly = 1) {
            nextFlow.evaluate(any(), experiment, user, any())
        }
    }
}