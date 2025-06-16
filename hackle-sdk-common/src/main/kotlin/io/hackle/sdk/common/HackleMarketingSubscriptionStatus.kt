package io.hackle.sdk.common

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