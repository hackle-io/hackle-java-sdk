package io.hackle.sdk.common

class HacklePushSubscriptionOperations private constructor(
    private val operations: Map<HacklePushSubscriptionType, HacklePushSubscriptionStatus>,
) {
    val size: Int get() = operations.size

    fun asMap(): Map<HacklePushSubscriptionType, HacklePushSubscriptionStatus> {
        return operations
    }

    class Builder {

        private val operations = hashMapOf<HacklePushSubscriptionType, HacklePushSubscriptionStatus>()

        fun global(status: HacklePushSubscriptionStatus) = apply {
            operations[HacklePushSubscriptionType.GLOBAL] = status
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
