package io.hackle.sdk.core.model

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.HacklePushSubscriptionOperations

fun HacklePushSubscriptionOperations.toEvent(): Event {
    val builder = Event.builder("\$push_subscriptions")
    for ((type, status) in asMap()) {
        builder.property(type.key, status)
    }
    return builder.build()
}
