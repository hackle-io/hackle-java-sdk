package io.hackle.sdk.common.marketing

/**
 * Marketing subscription operations.
 */
class HackleMarketingSubscriptionOperations private constructor(
    private val operations: Map<String, HackleMarketingSubscriptionStatus>,
) {
    val size: Int get() = operations.size

    fun asMap(): Map<String, HackleMarketingSubscriptionStatus> {
        return operations
    }

    class Builder {

        private val operations = hashMapOf<String, HackleMarketingSubscriptionStatus>()

        fun set(key: String, status: HackleMarketingSubscriptionStatus) = apply {
            operations[key] = status
        }

        fun global(status: HackleMarketingSubscriptionStatus) = apply {
            set(HackleMarketingSubscriptionType.GLOBAL.key, status)
        }

        fun build(): HackleMarketingSubscriptionOperations {
            return HackleMarketingSubscriptionOperations(operations)
        }
    }

    companion object {

        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}
