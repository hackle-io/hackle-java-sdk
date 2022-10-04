package io.hackle.sdk.core.evaluation

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.ParameterConfiguration
import io.hackle.sdk.core.model.Variation
import io.hackle.sdk.core.workspace.Workspace
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class EvaluationTest {

    @Nested
    inner class CreateTest {

        @Test
        fun `Variation, Reason 정보로 Evaluation을 생성한다`() {
            // given
            val variation = Variation(42, "C", false, 320)
            val reason = DecisionReason.TRAFFIC_ALLOCATED
            val config = mockk<ParameterConfiguration>()
            val workspace = mockk<Workspace> {
                every { getParameterConfigurationOrNull(any()) } returns config
            }

            // when
            val actual = Evaluation.of(workspace, variation, reason)

            // then
            expectThat(actual) isEqualTo Evaluation(42, "C", DecisionReason.TRAFFIC_ALLOCATED, config)
        }

        @Test
        fun `variationKey에 해당하는 Variation이 있으면 id까지 같이 설정한다`() {
            // given
            val variation = Variation(42, "C", false, 320)
            val config = mockk<ParameterConfiguration>()
            val workspace = mockk<Workspace> {
                every { getParameterConfigurationOrNull(any()) } returns config
            }

            val experiment = mockk<Experiment> {
                every { getVariationOrNull(any<String>()) } returns variation
            }

            // when
            val actual = Evaluation.of(workspace, experiment, "C", DecisionReason.TRAFFIC_NOT_ALLOCATED)

            // then
            expectThat(actual) isEqualTo Evaluation(42, "C", DecisionReason.TRAFFIC_NOT_ALLOCATED, config)
        }

        @Test
        fun `variationKey에 해당하는 Variation이 없으면 key만 설정한다`() {
            // given
            val config = mockk<ParameterConfiguration>()
            val workspace = mockk<Workspace> {
                every { getParameterConfigurationOrNull(any()) } returns config
            }
            val experiment = mockk<Experiment> {
                every { getVariationOrNull(any<String>()) } returns null
            }

            // when
            val actual = Evaluation.of(workspace, experiment, "C", DecisionReason.TRAFFIC_NOT_ALLOCATED)

            // then
            expectThat(actual) isEqualTo Evaluation(null, "C", DecisionReason.TRAFFIC_NOT_ALLOCATED, null)
        }
    }
}