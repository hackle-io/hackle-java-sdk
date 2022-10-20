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
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class EvaluationTest {

    @Nested
    inner class CreateTest {

        @Test
        fun `variationKey 에 대한 Variation 이 있으면 해당 Variation 정보로 Evaluation 을 생성한다`() {
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
        fun `Variation 의 parameterConfigurationId 로 ParameterConfiguration 을 찾을 수 없으면 예외 발생 `() {
            val variation = Variation(42, "C", false, 320)
            val reason = DecisionReason.TRAFFIC_ALLOCATED
            val config = mockk<ParameterConfiguration>()
            val workspace = mockk<Workspace> {
                every { getParameterConfigurationOrNull(any()) } returns null
            }

            // when

            val actual = assertThrows<IllegalArgumentException> { Evaluation.of(workspace, variation, reason) }

            // then
            expectThat(actual.message) isEqualTo "ParameterConfiguration[320]"
        }

        @Test
        fun `variationKey 에 해당하는 Variation 이 없으면 key 만 설정한다`() {
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