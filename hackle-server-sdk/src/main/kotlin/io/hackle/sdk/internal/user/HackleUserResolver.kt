package io.hackle.sdk.internal.user

import io.hackle.sdk.common.User
import io.hackle.sdk.core.user.HackleUser

internal class HackleUserResolver {

    fun resolveOrNull(user: User): HackleUser? {
        val hackleUser = HackleUser.of(user)
        if (hackleUser.identifiers.isEmpty()) {
            return null
        }
        return hackleUser
    }
}
