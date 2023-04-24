package io.hackle.sdk.core.event

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.RemoteConfigEvaluation
import io.hackle.sdk.core.model.EventType
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

        @Test
        fun `with`() {
            val event = UserEvent.Exposure(
                insertId = "insertId",
                timestamp = 42,
                user = HackleUser.builder().build(),
                experiment = experiment(),
                variationId = 320,
                variationKey = "B",
                decisionReason = DecisionReason.TRAFFIC_ALLOCATED,
                properties = mapOf("a" to "1")
            )
            val user = HackleUser.builder().build()
            expectThat(event.with(user)) {
                get { insertId } isEqualTo event.insertId
                get { timestamp } isEqualTo event.timestamp
                get { this.user } isSameInstanceAs user
                get { experiment } isEqualTo event.experiment
                get { variationId } isEqualTo event.variationId
                get { variationKey } isEqualTo event.variationKey
                get { decisionReason } isEqualTo event.decisionReason
                get { properties } isEqualTo event.properties
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

        @Test
        fun `with`() {
            val event = UserEvent.RemoteConfig(
                insertId = "insertId",
                timestamp = 42,
                user = HackleUser.builder().build(),
                parameter = mockk(),
                valueId = 320,
                decisionReason = DecisionReason.DEFAULT_RULE,
                properties = mapOf("1" to "2")
            )
            val user = HackleUser.builder().build()
            expectThat(event.with(user)) {
                get { insertId } isEqualTo event.insertId
                get { timestamp } isEqualTo event.timestamp
                get { this.user } isSameInstanceAs user
                get { parameter } isEqualTo event.parameter
                get { valueId } isEqualTo event.valueId
                get { decisionReason } isEqualTo event.decisionReason
                get { properties } isEqualTo event.properties
            }
        }
    }

    @Nested
    inner class TrackTest {

        @Test
        fun `track`() {

            val event = UserEvent.Track(
                insertId = "insertId",
                timestamp = 42,
                user = HackleUser.builder().build(),
                eventType = EventType.Custom(320, "event"),
                event = Event.of("event")
            )

            val user = HackleUser.builder().build()
            expectThat(event.with(user)) {
                get { insertId } isEqualTo event.insertId
                get { timestamp } isEqualTo event.timestamp
                get { this.user } isSameInstanceAs user
                get { eventType } isEqualTo event.eventType
                get { this.event } isEqualTo event.event
            }
        }
    }
}