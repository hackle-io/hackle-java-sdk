package io.hackle.sdk.internal.user

import io.hackle.sdk.common.User
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType

internal class HackleUserResolver {

    fun resolveOrNull(user: User): HackleUser? {
        val hackleUser = HackleUser.builder()
            .identifiers(user.identifiers)
            .identifier(IdentifierType.ID, user.id)
            .identifier(IdentifierType.USER, user.userId)
            .identifier(IdentifierType.DEVICE, user.deviceId)
            .properties(user.properties)
            .hackleProperties(user.hackleProperties)
            .build()
        if (hackleUser.identifiers.isEmpty()) {
            return null
        }
        return hackleUser
    }
}
