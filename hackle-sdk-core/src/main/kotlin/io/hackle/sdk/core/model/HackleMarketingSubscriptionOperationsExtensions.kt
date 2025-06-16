package io.hackle.sdk.core.model

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.HackleMarketingSubscriptionOperations

fun HackleMarketingSubscriptionOperations.toPushSubscriptionEvent(): Event {
    val builder = Event.builder("\$push_subscriptions")
    for ((type, status) in asMap()) {
        builder.property(type, status.key)
    }
    return builder.build()
}

fun HackleMarketingSubscriptionOperations.toSmsSubscriptionEvent(): Event {
    val builder = Event.builder("\$sms_subscriptions")
    for ((type, status) in asMap()) {
        builder.property(type, status.key)
    }
    return builder.build()
}

fun HackleMarketingSubscriptionOperations.toKakaoSubscriptionEvent(): Event {
    val builder = Event.builder("\$kakao_subscriptions")
    for ((type, status) in asMap()) {
        builder.property(type, status.key)
    }
    return builder.build()
}
