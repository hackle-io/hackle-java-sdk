package io.hackle.sdk.common

class HackleMarketingSubscriptionOperations private constructor(

    /**
     * MarketingSubscriptionStatus -> Status
     */
    private val operations: Map<String, HackleMarketingSubscriptionStatus>,
) {
    val size: Int get() = operations.size

    fun asMap(): Map<String, HackleMarketingSubscriptionStatus> {
        return operations
    }

    class Builder {

        private val operations = hashMapOf<String, HackleMarketingSubscriptionStatus>()

        fun global(status: HackleMarketingSubscriptionStatus) = apply {
            operations[HackleMarketingSubscriptionType.GLOBAL.key] = status
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
