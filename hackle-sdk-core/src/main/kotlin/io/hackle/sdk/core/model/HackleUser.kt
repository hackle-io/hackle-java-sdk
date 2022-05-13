package io.hackle.sdk.core.model

import io.hackle.sdk.common.Identifiers
import io.hackle.sdk.common.User

data class HackleUser(
    val identifiers: Identifiers,
    val properties: Map<String, Any>,
    val hackleProperties: Map<String, Any>
) {

    companion object {
        fun of(id: String): HackleUser {
            return of(User.of(id), emptyMap())
        }

        fun of(user: User, hackleProperties: Map<String, Any> = emptyMap()): HackleUser {
            return HackleUser(
                identifiers = user.identifiers,
                properties = user.properties,
                hackleProperties = hackleProperties
            )
        }
    }
}
