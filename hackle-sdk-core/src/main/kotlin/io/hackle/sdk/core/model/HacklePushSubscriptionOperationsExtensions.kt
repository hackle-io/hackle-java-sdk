package io.hackle.sdk.core.model

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.HacklePushSubscriptionOperations

fun HacklePushSubscriptionOperations.toEvent(): Event {
    val builder = Event.builder("\$push_subscriptions")
    for ((type, state) in asMap()) {
        builder.property(type.key, state)
    }
    return builder.build()
}
