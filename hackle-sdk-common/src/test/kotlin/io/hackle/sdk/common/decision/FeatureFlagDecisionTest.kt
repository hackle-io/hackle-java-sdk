package io.hackle.sdk.common.decision

import io.hackle.sdk.common.ParameterConfig
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

internal class FeatureFlagDecisionTest {

    @Test
    fun `on`() {
        expectThat(FeatureFlagDecision.on(DecisionReason.DEFAULT_RULE)) {
            get { isOn }.isTrue()
            get { reason } isEqualTo DecisionReason.DEFAULT_RULE
            get { config } isEqualTo ParameterConfig.empty()
        }

        val parameterConfig = mockk<ParameterConfig>()
        expectThat(FeatureFlagDecision.on(DecisionReason.DEFAULT_RULE, parameterConfig)) {
            get { isOn }.isTrue()
            get { reason } isEqualTo DecisionReason.DEFAULT_RULE
            get { config } isEqualTo parameterConfig
        }
    }

    @Test
    fun `off`() {
        expectThat(FeatureFlagDecision.off(DecisionReason.DEFAULT_RULE)) {
            get { isOn }.isFalse()
            get { reason } isEqualTo DecisionReason.DEFAULT_RULE
            get { config } isEqualTo ParameterConfig.empty()
        }

        val parameterConfig = mockk<ParameterConfig>()
        expectThat(FeatureFlagDecision.off(DecisionReason.DEFAULT_RULE, parameterConfig)) {
            get { isOn }.isFalse()
            get { reason } isEqualTo DecisionReason.DEFAULT_RULE
            get { config } isEqualTo parameterConfig
        }
    }
}