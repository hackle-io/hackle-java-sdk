package io.hackle.sdk.common

@Deprecated("Use HackleSubscriptionOperations instead")
class HacklePushSubscriptionOperations private constructor(

    /**
     * PushSubscriptionType -> Status
     */
    private val operations: Map<String, HacklePushSubscriptionStatus>,
) {
    val size: Int get() = operations.size

    fun asMap(): Map<String, HacklePushSubscriptionStatus> {
        return operations
    }

    class Builder {

        private val operations = hashMapOf<String, HacklePushSubscriptionStatus>()

        fun global(status: HacklePushSubscriptionStatus) = apply {
            operations[HacklePushSubscriptionType.GLOBAL.key] = status
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
