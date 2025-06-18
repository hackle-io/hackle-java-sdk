package io.hackle.sdk.core.model

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.channel.HackleSubscriptionOperations

fun HackleSubscriptionOperations.toEvent(key: String): Event {
    val builder = Event.builder(key)
    for ((type, status) in asMap()) {
        builder.property(type, status.key)
    }
    return builder.build()
}
