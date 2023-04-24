package io.hackle.sdk.common.decision

import io.hackle.sdk.common.ParameterConfig
import io.hackle.sdk.common.Variation
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class DecisionTest {

    @Test
    fun `decision`() {
        expectThat(Decision.of(Variation.A, DecisionReason.TRAFFIC_ALLOCATED)) {
            get { variation } isEqualTo Variation.A
            get { reason } isEqualTo DecisionReason.TRAFFIC_ALLOCATED
            get { config } isEqualTo ParameterConfig.empty()
        }

        val parameterConfig = mockk<ParameterConfig>()
        expectThat(Decision.of(Variation.A, DecisionReason.TRAFFIC_ALLOCATED, parameterConfig)) {
            get { variation } isEqualTo Variation.A
            get { reason } isEqualTo DecisionReason.TRAFFIC_ALLOCATED
            get { config } isEqualTo parameterConfig
        }
    }
}