package io.hackle.sdk.common

enum class HacklePushSubscriptionOperation(val key: String) {
    GLOBAl("\$global");

    companion object {

        private val OPERATIONS = values().associateBy { it.key }

        @JvmStatic
        fun from(key: String): HacklePushSubscriptionOperation {
            return requireNotNull(fromOrNull(key)) { "HacklePushSubscriptionOperation[$key]" }
        }

        @JvmStatic
        fun fromOrNull(key: String): HacklePushSubscriptionOperation? {
            return OPERATIONS[key]
        }
    }
}