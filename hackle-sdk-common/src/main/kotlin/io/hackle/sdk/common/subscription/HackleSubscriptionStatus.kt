package io.hackle.sdk.common.subscription

/**
 * An enum that represents the status of a subscription.
 */
enum class HackleSubscriptionStatus(val key: String) {
    SUBSCRIBED("SUBSCRIBED"),
    UNSUBSCRIBED("UNSUBSCRIBED"),
    UNKNOWN("UNKNOWN"),
}
