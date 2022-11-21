package io.hackle.sdk.core.event

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.RemoteConfigEvaluation
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.ParameterConfiguration
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.user.HackleUser
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs

internal class UserEventTest {

    @Nested
    inner class ExposureTest {

        @Test
        fun `parameterConfigurationId 를 속성으로 설정한다`() {
            // given
            val parameterConfiguration = ParameterConfiguration(42, emptyMap())
            val evaluation = Evaluation(320, "B", DecisionReason.TRAFFIC_ALLOCATED, parameterConfiguration)
            val experiment = mockk<Experiment>()
            val user = HackleUser.of("test_id")

            // when
            val actual = UserEvent.exposure(experiment, user, evaluation)

            // then
            expectThat(actual)
                .isA<UserEvent.Exposure>()
                .get { properties["\$parameterConfigurationId"] }
                .isNotNull()
                .isEqualTo(42L)
        }
    }

    @Nested
    inner class RemoteConfigTest {

        @Test
        fun `create`() {
            // given
            val remoteConfigParameter = mockk<RemoteConfigParameter>()
            val user = HackleUser.of("id")
            val evaluation = RemoteConfigEvaluation(
                42, "remote config value", DecisionReason.DEFAULT_RULE, mapOf(
                    "request.valueType" to "STRING",
                    "request.defaultValue" to "default value",
                    "return.value" to "remote config value",
                )
            )

            // when
            val remoteConfigEvent = UserEvent.remoteConfig(remoteConfigParameter, user, evaluation)

            // then
            expectThat(remoteConfigEvent)
                .isA<UserEvent.RemoteConfig>()
                .and {
                    get { parameter } isSameInstanceAs remoteConfigParameter
                    get { valueId } isEqualTo 42
                    get { decisionReason } isEqualTo DecisionReason.DEFAULT_RULE
                    get { properties["request.valueType"] } isEqualTo "STRING"
                    get { properties["request.defaultValue"] } isEqualTo "default value"
                    get { properties["return.value"] } isEqualTo "remote config value"
                }
        }
    }
}