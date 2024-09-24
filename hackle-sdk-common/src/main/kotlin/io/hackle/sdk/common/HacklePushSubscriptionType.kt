package io.hackle.sdk.common

enum class HacklePushSubscriptionType(val key: String) {
    GLOBAL("\$global");

    companion object {

        private val OPERATIONS = values().associateBy { it.key }

        @JvmStatic
        fun from(key: String): HacklePushSubscriptionType {
            return requireNotNull(fromOrNull(key)) { "HacklePushSubscriptionType[$key]" }
        }

        @JvmStatic
        fun fromOrNull(key: String): HacklePushSubscriptionType? {
            return OPERATIONS[key]
        }
    }
}