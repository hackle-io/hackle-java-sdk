package io.hackle.sdk.common

/**
 * An enum that represents the status of a marketing subscription.
 */
enum class HackleMarketingSubscriptionStatus(val key: String) {
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