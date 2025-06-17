package io.hackle.sdk.common.marketing

/**
 * An enum that represents the status of a marketing subscription.
 */
enum class HackleMarketingSubscriptionStatus(val key: String) {
    /**
     * Receiving marketing message
     */
    SUBSCRIBED("SUBSCRIBED"),

    /**
     * Opting out of receiving marketing messages
     */
    UNSUBSCRIBED("UNSUBSCRIBED"),

    /**
     * Unknown. initial state
     *
     * User has not decided whether to consent to receiving marketing
     *
     * Receive marketing messages even if the subscription status is unknown.
     */
    UNKNOWN("UNKNOWN"),
}