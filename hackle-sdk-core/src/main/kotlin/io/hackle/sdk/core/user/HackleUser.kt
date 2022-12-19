package io.hackle.sdk.core.user

import io.hackle.sdk.common.User

data class HackleUser internal constructor(
    val identifiers: Map<String, String>,
    val properties: Map<String, Any>,
    val hackleProperties: Map<String, Any>
) {

    val userId: String? get() = identifiers[IdentifierType.USER.key]
    val deviceId: String? get() = identifiers[IdentifierType.DEVICE.key]
    val sessionId: String? get() = identifiers[IdentifierType.SESSION.key]

    companion object {
        fun of(id: String): HackleUser {
            return of(User.of(id), emptyMap())
        }

        fun of(user: User, hackleProperties: Map<String, Any> = emptyMap()): HackleUser {
            val identifiers = IdentifiersBuilder()
                .add(user.identifiers)
                .add(IdentifierType.ID, user.id)
                .add(IdentifierType.USER, user.userId)
                .add(IdentifierType.DEVICE, user.deviceId)
                .build()

            return HackleUser(
                identifiers = identifiers,
                properties = user.properties,
                hackleProperties = hackleProperties
            )
        }
    }
}
