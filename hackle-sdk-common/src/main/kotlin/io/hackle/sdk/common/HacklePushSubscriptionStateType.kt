package io.hackle.sdk.common

/**
 * An enum that represents the state of a push subscription.
 */
enum class HacklePushSubscriptionStateType(val key: String) {

   /*
    * Subscribe push message
    */
    SUBSCRIBED("SUBSCRIBED"),

    /*
    * Unsubscribe push message
    */
    UNSUBSCRIBED("UNSUBSCRIBED"),

    /*
    * Unable to determine state
    */
    UNKNOWN("UNKNOWN"),
}