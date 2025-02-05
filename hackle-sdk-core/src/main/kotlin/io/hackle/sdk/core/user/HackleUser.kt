package io.hackle.sdk.core.user

import io.hackle.sdk.common.IdentifiersBuilder
import io.hackle.sdk.common.PropertiesBuilder
import io.hackle.sdk.common.User
import io.hackle.sdk.core.model.Cohort
import io.hackle.sdk.core.model.TargetEvent
import java.util.*

data class HackleUser internal constructor(
    val identifiers: Map<String, String>,
    val properties: Map<String, Any>,
    val hackleProperties: Map<String, Any>,
    val cohorts: List<Cohort>,
    val targetEvents: List<TargetEvent>
) {

    val id: String? get() = identifiers[IdentifierType.ID.key]
    val userId: String? get() = identifiers[IdentifierType.USER.key]
    val deviceId: String? get() = identifiers[IdentifierType.DEVICE.key]
    val sessionId: String? get() = identifiers[IdentifierType.SESSION.key]

    fun toBuilder(): Builder {
        return Builder(this)
    }

    class Builder internal constructor() {

        private val identifiers: IdentifiersBuilder = IdentifiersBuilder()
        private val properties: PropertiesBuilder = PropertiesBuilder()
        private val hackleProperties: PropertiesBuilder = PropertiesBuilder()
        private val cohorts = mutableListOf<Cohort>()
        private val targetEvents = mutableListOf<TargetEvent>()

        internal constructor(user: HackleUser) : this() {
            identifiers.add(user.identifiers)
            properties.add(user.properties)
            hackleProperties.add(user.hackleProperties)
            cohorts.addAll(user.cohorts)
            targetEvents.addAll(user.targetEvents)
        }

        fun identifiers(identifiers: Map<String, String>, overwrite: Boolean = true) =
            apply { this.identifiers.add(identifiers, overwrite) }

        fun identifier(type: String, value: String?, overwrite: Boolean = true) =
            apply { this.identifiers.add(type, value, overwrite) }

        fun identifier(type: IdentifierType, value: String?, overwrite: Boolean = true) =
            apply { this.identifiers.add(type.key, value, overwrite) }

        fun properties(properties: Map<String, Any>) = apply { this.properties.add(properties) }
        fun property(key: String, value: Any) = apply { this.properties.add(key, value) }

        fun hackleProperties(properties: Map<String, Any>) = apply { this.hackleProperties.add(properties) }
        fun hackleProperty(key: String, value: Any) = apply { this.hackleProperties.add(key, value) }

        fun cohort(cohort: Cohort) = apply { this.cohorts.add(cohort) }
        fun cohorts(cohorts: List<Cohort>) = apply { this.cohorts.addAll(cohorts) }

        fun targetEvent(targetEvent: TargetEvent) = apply { this.targetEvents.add(targetEvent) }
        fun targetEvents(targetEvents: List<TargetEvent>) = apply { this.targetEvents.addAll(targetEvents) }

        fun build(): HackleUser {
            return HackleUser(
                identifiers = identifiers.build(),
                properties = properties.build(),
                hackleProperties = hackleProperties.build(),
                cohorts = Collections.unmodifiableList(cohorts),
                targetEvents = Collections.unmodifiableList(targetEvents)
            )
        }
    }

    companion object {

        fun builder(): Builder {
            return Builder()
        }

        @Deprecated("Use HackleUser.builder() instead.")
        fun of(id: String): HackleUser {
            return builder()
                .identifier(IdentifierType.ID, id)
                .build()
        }

        @Deprecated("Use HackleUser.builder() instead.")
        fun of(user: User, hackleProperties: Map<String, Any> = emptyMap()): HackleUser {
            return builder()
                .identifiers(user.identifiers)
                .identifier(IdentifierType.ID, user.id)
                .identifier(IdentifierType.USER, user.userId)
                .identifier(IdentifierType.DEVICE, user.deviceId)
                .properties(user.properties)
                .hackleProperties(hackleProperties)
                .build()
        }
    }
}
