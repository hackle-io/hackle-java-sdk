package io.hackle.sdk.core.decision

import io.hackle.sdk.common.ParameterConfig
import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.model.ParameterConfiguration
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.RemoteConfigs
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isSameInstanceAs
import strikt.assertions.isTrue

internal class DecisionsTest {

    @Nested
    inner class RemoteConfigToDecisionTest {

        @Test
        fun `value 가 요청 타입으로 캐스팅되면 해당 값으로 결정한다`() {
            val evaluation = RemoteConfigs.evaluation(
                result = RemoteConfigs.result(
                    reason = DecisionReason.TARGET_RULE_MATCH,
                    value = RemoteConfigs.value(rawValue = "foo")
                )
            )

            val decision = evaluation.toDecision(ValueType.STRING, "default")

            expectThat(decision) {
                get { value } isEqualTo "foo"
                get { reason } isEqualTo DecisionReason.TARGET_RULE_MATCH
            }
        }

        @Test
        fun `value 가 없으면 defaultValue 로 결정한다`() {
            val evaluation = RemoteConfigs.evaluation(
                result = RemoteConfigs.result(
                    reason = DecisionReason.REMOTE_CONFIG_PARAMETER_NOT_FOUND,
                    value = null
                )
            )

            val decision = evaluation.toDecision(ValueType.STRING, "default")

            expectThat(decision) {
                get { value } isEqualTo "default"
                get { reason } isEqualTo DecisionReason.REMOTE_CONFIG_PARAMETER_NOT_FOUND
            }
        }

        @Test
        fun `value 가 요청 타입으로 캐스팅되지 않으면 defaultValue 로 결정한다`() {
            val evaluation = RemoteConfigs.evaluation(
                result = RemoteConfigs.result(
                    reason = DecisionReason.TYPE_MISMATCH,
                    value = RemoteConfigs.value(rawValue = "not_a_number")
                )
            )

            val decision = evaluation.toDecision(ValueType.NUMBER, 999.0)

            expectThat(decision) {
                get { value } isEqualTo 999.0
                get { reason } isEqualTo DecisionReason.TYPE_MISMATCH
            }
        }
    }

    @Nested
    inner class ExperimentToDecisionTest {

        @Test
        fun `variation, reason, config, experiment 로 Decision 을 생성한다`() {
            val parameterConfiguration = ParameterConfiguration(42, mapOf("key" to "value"))
            val experiment = Experiments.config(id = 1, key = 10)
            val evaluation = Experiments.evaluation(
                entity = experiment,
                result = Experiments.result(
                    reason = DecisionReason.TRAFFIC_ALLOCATED,
                    variation = Experiments.variation(key = "B", parameterConfiguration = parameterConfiguration)
                )
            )

            val decision = evaluation.toDecision()

            expectThat(decision) {
                get { variation } isEqualTo Variation.B
                get { reason } isEqualTo DecisionReason.TRAFFIC_ALLOCATED
                get { config } isEqualTo parameterConfiguration
                get { experiment } isSameInstanceAs experiment
            }
        }

        @Test
        fun `parameterConfiguration 이 없으면 빈 config 로 생성한다`() {
            val evaluation = Experiments.evaluation(
                result = Experiments.result(
                    variation = Experiments.variation(key = "A", parameterConfiguration = null)
                )
            )

            val decision = evaluation.toDecision()

            expectThat(decision.config) isEqualTo ParameterConfig.empty()
        }
    }

    @Nested
    inner class ExperimentToFeatureFlagDecisionTest {

        @Test
        fun `control variation 이면 off 로 결정한다`() {
            val evaluation = Experiments.evaluation(
                result = Experiments.result(
                    reason = DecisionReason.DEFAULT_RULE,
                    variation = Experiments.variation(key = "A", parameterConfiguration = null)
                )
            )

            val decision = evaluation.toFeatureFlagDecision()

            expectThat(decision) {
                get { isOn }.isFalse()
                get { reason } isEqualTo DecisionReason.DEFAULT_RULE
                get { config } isEqualTo ParameterConfig.empty()
            }
        }

        @Test
        fun `control 이 아닌 variation 이면 on 으로 결정한다`() {
            val parameterConfiguration = ParameterConfiguration(42, mapOf("key" to "value"))
            val experiment = Experiments.config(id = 1, key = 10)
            val evaluation = Experiments.evaluation(
                entity = experiment,
                result = Experiments.result(
                    reason = DecisionReason.TARGET_RULE_MATCH,
                    variation = Experiments.variation(key = "B", parameterConfiguration = parameterConfiguration)
                )
            )

            val decision = evaluation.toFeatureFlagDecision()

            expectThat(decision) {
                get { isOn }.isTrue()
                get { reason } isEqualTo DecisionReason.TARGET_RULE_MATCH
                get { config } isEqualTo parameterConfiguration
                get { featureFlag } isSameInstanceAs experiment
            }
        }
    }
}
