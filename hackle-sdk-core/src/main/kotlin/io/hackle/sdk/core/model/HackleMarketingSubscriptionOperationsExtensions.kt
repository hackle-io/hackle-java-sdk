package io.hackle.sdk.core.model

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.marketing.HackleMarketingChannel
import io.hackle.sdk.common.marketing.HackleMarketingSubscriptionOperations

fun HackleMarketingSubscriptionOperations.toSubscriptionEvent(channel: HackleMarketingChannel): Event {
    val builder = Event.builder(channel.subscriptionEventKey())
    for ((type, status) in asMap()) {
        builder.property(type, status.key)
    }
    return builder.build()
}
