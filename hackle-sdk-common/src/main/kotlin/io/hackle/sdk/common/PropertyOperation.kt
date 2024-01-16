package io.hackle.sdk.common

enum class PropertyOperation(val key: String) {
    SET("\$set"),
    SET_ONCE("\$setOnce"),
    UNSET("\$unset"),
    INCREMENT("\$increment"),
    APPEND("\$append"),
    APPEND_ONCE("\$appendOnce"),
    PREPEND("\$prepend"),
    PREPEND_ONCE("\$prependOnce"),
    REMOVE("\$remove"),
    CLEAR_ALL("\$clearAll");

    companion object {

        private val OPERATIONS = values().associateBy { it.key }

        @JvmStatic
        fun from(key: String): PropertyOperation {
            return requireNotNull(fromOrNull(key)) { "PropertyOperation[$key]" }
        }

        @JvmStatic
        fun fromOrNull(key: String): PropertyOperation? {
            return OPERATIONS[key]
        }
    }
}
