package io.hackle.sdk.internal.user

import io.hackle.sdk.common.Identifiers
import io.hackle.sdk.common.User
import io.hackle.sdk.core.model.HackleUser

internal class HackleUserResolver {

    fun resolveOrNull(user: User): HackleUser? {

        val identifiers = user.identifiers.toBuilder()
            .add(Identifiers.Type.ID, user.id)
            .add(Identifiers.Type.DEVICE, user.deviceId)
            .add(Identifiers.Type.USER, user.userId)
            .build()

        if (identifiers.isEmpty) {
            return null
        }

        return HackleUser(
            identifiers = identifiers,
            properties = user.properties,
            hackleProperties = emptyMap()
        )
    }
}
