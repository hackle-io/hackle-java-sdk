package io.hackle.sdk.core.model

import io.hackle.sdk.common.User

/**
 * @author Yong
 */
sealed class Experiment {

    abstract val id: Long
    abstract val key: Long

    class Running(
        override val id: Long,
        override val key: Long,
        val bucket: Bucket,
        private val variations: Map<Long, Variation>,
        private val overrides: Map<String, Long>
    ) : Experiment() {

        fun getVariationOrNull(variationId: Long): Variation? {
            return variations[variationId]
        }

        fun getOverriddenVariationOrNull(user: User): Variation? {
            val overriddenVariationId = overrides[user.id] ?: return null
            val overriddenVariation = getVariationOrNull(overriddenVariationId)
            return requireNotNull(overriddenVariation) { "experiment[$id] variation[$overriddenVariationId]" }
        }
    }

    class Completed(
        override val id: Long,
        override val key: Long,
        val winnerVariationKey: String
    ) : Experiment()
}
