package io.hackle.sdk.core.model

import io.hackle.sdk.common.User

data class HackleUser(
    val id: String,
    val properties: Map<String, Any>,
    val hackleProperties: Map<String, Any>
) {

    companion object {
        fun of(userId: String): HackleUser {
            return HackleUser(
                id = userId,
                properties = emptyMap(),
                hackleProperties = emptyMap()
            )
        }

        fun of(user: User, hackleProperties: Map<String, Any> = emptyMap()): HackleUser {
            return HackleUser(
                id = user.id,
                properties = user.properties,
                hackleProperties = hackleProperties
            )
        }
    }
}
