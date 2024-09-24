package io.hackle.sdk.common

class HacklePushSubscriptionOperations (
    private val operations: Map<HacklePushSubscriptionOperation, String>
) {
    val size: Int get() = operations.size

    fun asMap(): Map<HacklePushSubscriptionOperation, String> {
        return operations
    }

    class Builder {

        private val operations = hashMapOf<HacklePushSubscriptionOperation, String>()

        fun  setGlobal(state: HacklePushSubscriptionStateType) {
            operations[HacklePushSubscriptionOperation.GLOBAl] = state.key
        }

        fun build(): HacklePushSubscriptionOperations {
            return HacklePushSubscriptionOperations(operations)
        }
    }
}