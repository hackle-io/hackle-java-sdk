package io.hackle.sdk.core.event

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.RemoteConfigEvaluation
import io.hackle.sdk.core.model.ParameterConfiguration
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.model.experiment
import io.hackle.sdk.core.user.HackleUser
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

internal class UserEventTest {

    @Nested
    inner class ExposureTest {

        @Test
        fun `create`() {
            // given

            val parameterConfiguration = ParameterConfiguration(42, emptyMap())

            val evaluation = ExperimentEvaluation(
                DecisionReason.TRAFFIC_ALLOCATED,
                emptyList(),
                experiment(),
                42,
                "B",
                parameterConfiguration
            )
            val user = HackleUser.of("test_id")

            // when
            val actual = UserEvent.exposure(user, evaluation, mapOf("a" to "1"), 320)

            // then
            expectThat(actual).isA<UserEvent.Exposure>().and {
                get { properties["a"] } isEqualTo "1"
                get { timestamp } isEqualTo 320
                get { decisionReason } isEqualTo DecisionReason.TRAFFIC_ALLOCATED
            }
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
                DecisionReason.DEFAULT_RULE,
                emptyList(),
                remoteConfigParameter,
                42,
                "remote config value",
                mapOf("a" to "1")
            )

            // when
            val remoteConfigEvent = UserEvent.remoteConfig(user, evaluation, mapOf("b" to "2"), 320)

            // then
            expectThat(remoteConfigEvent)
                .isA<UserEvent.RemoteConfig>()
                .and {
                    get { parameter } isSameInstanceAs remoteConfigParameter
                    get { valueId } isEqualTo 42
                    get { decisionReason } isEqualTo DecisionReason.DEFAULT_RULE
                    get { properties["b"] } isEqualTo "2"
                }
        }
    }
}