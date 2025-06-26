package io.hackle.sdk.common.subscription

import java.util.*

/**
 * subscription operations.
 */
class HackleSubscriptionOperations private constructor(
    private val operations: Map<String, HackleSubscriptionStatus>,
) {
    val size: Int get() = operations.size

    fun asMap(): Map<String, HackleSubscriptionStatus> {
        return operations
    }

    class Builder {

        private val operations = hashMapOf<String, HackleSubscriptionStatus>()

        fun marketing(status: HackleSubscriptionStatus) = apply {
            operations["\$marketing"] = status
        }

        fun information(status: HackleSubscriptionStatus) = apply {
            operations["\$information"] = status
        }

        fun custom(key: String, status: HackleSubscriptionStatus) = apply {
            operations[key] = status
        }

        fun build(): HackleSubscriptionOperations {
            return HackleSubscriptionOperations(Collections.unmodifiableMap(operations))
        }
    }

    companion object {

        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}
