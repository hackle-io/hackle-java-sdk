package io.hackle.sdk.common

/**
 * An enum that represents the status of a push subscription.
 */
@Deprecated("Use HackleMarketingSubscriptionStatus instead")
enum class HacklePushSubscriptionStatus(val key: String) {

    /**
     * Subscribe push message
     */
    SUBSCRIBED("SUBSCRIBED"),

    /**
     * Unsubscribe push message
     */
    UNSUBSCRIBED("UNSUBSCRIBED"),

    /**
     * Unable to determine status
     */
    UNKNOWN("UNKNOWN"),
}
