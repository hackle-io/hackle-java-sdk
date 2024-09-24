package io.hackle.sdk.common

class HacklePushSubscriptionOperations private constructor(
    private val operations: Map<HacklePushSubscriptionType, String>,
) {
    val size: Int get() = operations.size

    fun asMap(): Map<HacklePushSubscriptionType, String> {
        return operations
    }

    class Builder {

        private val operations = hashMapOf<HacklePushSubscriptionType, String>()

        fun global(status: HacklePushSubscriptionStatus) = apply {
            operations[HacklePushSubscriptionType.GLOBAL] = status.key
        }

        fun build(): HacklePushSubscriptionOperations {
            return HacklePushSubscriptionOperations(operations)
        }
    }

    companion object {

        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}
