package io.hackle.sdk.core.event

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.RemoteConfigs
import io.hackle.sdk.core.support.Workspaces
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

internal class UserEventTest {

    private val user = HackleUser.builder().identifier(IdentifierType.ID, "user").build()

    @Nested
    inner class ExposureTest {

        @Test
        fun `create`() {
            // given
            val variation = Experiments.variation(id = 42, key = "B")
            val experiment = Experiments.config(variations = listOf(variation))
            val evaluation = Experiments.evaluation(
                entity = experiment,
                result = Experiments.result(DecisionReason.TRAFFIC_ALLOCATED, variation)
            )
            val workspace = Workspaces.config(
                metadata = Workspaces.configMetadata(modifiedAt = "2024-01-01")
            )

            // when
            val actual = UserEvent.exposure(320, user, workspace, evaluation, mapOf("a" to "1"))

            // then
            expectThat(actual).isA<UserEvent.Exposure>().and {
                get { timestamp } isEqualTo 320L
                get { this.user } isSameInstanceAs user
                get { this.experiment } isSameInstanceAs experiment
                get { variationId } isEqualTo 42L
                get { variationKey } isEqualTo "B"
                get { decisionReason } isEqualTo DecisionReason.TRAFFIC_ALLOCATED
                get { properties } isEqualTo mapOf("a" to "1")
                get { internalProperties } isEqualTo mapOf("config_modified_at" to "2024-01-01")
            }
        }

        @Test
        fun `with`() {
            val event = UserEvent.Exposure(
                insertId = "insertId",
                timestamp = 42,
                user = user,
                properties = mapOf("a" to "1"),
                internalProperties = mapOf("b" to "2"),
                experiment = Experiments.config(),
                variationId = 320,
                variationKey = "B",
                decisionReason = DecisionReason.TRAFFIC_ALLOCATED,
            )
            val newUser = HackleUser.builder().identifier(IdentifierType.ID, "new_user").build()
            expectThat(event.with(newUser)) {
                get { insertId } isEqualTo event.insertId
                get { timestamp } isEqualTo event.timestamp
                get { this.user } isSameInstanceAs newUser
                get { properties } isEqualTo event.properties
                get { internalProperties } isEqualTo event.internalProperties
                get { experiment } isEqualTo event.experiment
                get { variationId } isEqualTo event.variationId
                get { variationKey } isEqualTo event.variationKey
                get { decisionReason } isEqualTo event.decisionReason
            }
        }
    }

    @Nested
    inner class TrackTest {

        @Test
        fun `create`() {
            // given
            val event = Event.of("test_event")
            val workspace = Workspaces.config(
                metadata = Workspaces.configMetadata(modifiedAt = "2024-01-01")
            )

            // when
            val actual = UserEvent.track(42, user, workspace, event)

            // then
            expectThat(actual).isA<UserEvent.Track>().and {
                get { timestamp } isEqualTo 42L
                get { this.user } isSameInstanceAs user
                get { this.event } isSameInstanceAs event
                get { properties } isEqualTo event.properties
                get { internalProperties } isEqualTo mapOf("config_modified_at" to "2024-01-01")
            }
        }

        @Test
        fun `create - workspace 가 없으면 internalProperties 는 비어있다`() {
            // when
            val actual = UserEvent.track(42, user, null, Event.of("test_event"))

            // then
            expectThat(actual).isA<UserEvent.Track>().and {
                get { internalProperties } isEqualTo emptyMap()
            }
        }

        @Test
        fun `with`() {
            val event = UserEvent.Track(
                insertId = "insertId",
                timestamp = 42,
                user = user,
                internalProperties = mapOf("b" to "2"),
                event = Event.of("event"),
            )
            val newUser = HackleUser.builder().identifier(IdentifierType.ID, "new_user").build()
            expectThat(event.with(newUser)) {
                get { insertId } isEqualTo event.insertId
                get { timestamp } isEqualTo event.timestamp
                get { this.user } isSameInstanceAs newUser
                get { internalProperties } isEqualTo event.internalProperties
                get { this.event } isEqualTo event.event
            }
        }
    }

    @Nested
    inner class RemoteConfigTest {

        @Test
        fun `create`() {
            // given
            val parameter = RemoteConfigs.config()
            val evaluation = RemoteConfigs.evaluation(
                parameter = parameter,
                result = RemoteConfigs.result(
                    reason = DecisionReason.DEFAULT_RULE,
                    value = RemoteConfigs.value(id = 42, rawValue = "remote config value")
                )
            )
            val workspace = Workspaces.config(
                metadata = Workspaces.configMetadata(modifiedAt = "2024-01-01")
            )

            // when
            val actual = UserEvent.remoteConfig(320, user, workspace, evaluation, mapOf("b" to "2"))

            // then
            expectThat(actual).isA<UserEvent.RemoteConfig>().and {
                get { timestamp } isEqualTo 320L
                get { this.user } isSameInstanceAs user
                get { this.parameter } isSameInstanceAs parameter
                get { valueId } isEqualTo 42L
                get { decisionReason } isEqualTo DecisionReason.DEFAULT_RULE
                get { properties } isEqualTo mapOf("b" to "2")
                get { internalProperties } isEqualTo mapOf("config_modified_at" to "2024-01-01")
            }
        }

        @Test
        fun `create - value 가 없으면 valueId 는 null`() {
            // given
            val evaluation = RemoteConfigs.evaluation(
                result = RemoteConfigs.result(reason = DecisionReason.IDENTIFIER_NOT_FOUND, value = null)
            )

            // when
            val actual = UserEvent.remoteConfig(320, user, Workspaces.config(), evaluation, emptyMap())

            // then
            expectThat(actual).isA<UserEvent.RemoteConfig>().and {
                get { valueId }.isNull()
                get { decisionReason } isEqualTo DecisionReason.IDENTIFIER_NOT_FOUND
            }
        }

        @Test
        fun `with`() {
            val event = UserEvent.RemoteConfig(
                insertId = "insertId",
                timestamp = 42,
                user = user,
                properties = mapOf("1" to "2"),
                internalProperties = mapOf("b" to "2"),
                parameter = RemoteConfigs.config(),
                valueId = 320,
                decisionReason = DecisionReason.DEFAULT_RULE,
            )
            val newUser = HackleUser.builder().identifier(IdentifierType.ID, "new_user").build()
            expectThat(event.with(newUser)) {
                get { insertId } isEqualTo event.insertId
                get { timestamp } isEqualTo event.timestamp
                get { this.user } isSameInstanceAs newUser
                get { properties } isEqualTo event.properties
                get { internalProperties } isEqualTo event.internalProperties
                get { parameter } isEqualTo event.parameter
                get { valueId } isEqualTo event.valueId
                get { decisionReason } isEqualTo event.decisionReason
            }
        }
    }
}
