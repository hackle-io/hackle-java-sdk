package io.hackle.sdk.common

class HacklePushSubscriptionOperations (
    private val operations: Map<HacklePushSubscriptionType, String>
) {
    val size: Int get() = operations.size

    fun asMap(): Map<HacklePushSubscriptionType, String> {
        return operations
    }

    class Builder {

        private val operations = hashMapOf<HacklePushSubscriptionType, String>()

        fun  global(state: HacklePushSubscriptionState) {
            operations[HacklePushSubscriptionType.GLOBAL] = state.key
        }

        fun build(): HacklePushSubscriptionOperations {
            return HacklePushSubscriptionOperations(operations)
        }
    }
}