package io.hackle.sdk.core.client

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.User
import io.hackle.sdk.common.Variation

/**
 * @author Yong
 */
interface HackleClient : AutoCloseable {
    fun variation(experimentKey: Long, user: User, defaultVariation: Variation): Variation
    fun track(event: Event, user: User)
}
