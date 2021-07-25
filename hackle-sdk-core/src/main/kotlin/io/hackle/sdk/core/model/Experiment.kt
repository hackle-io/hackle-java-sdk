package io.hackle.sdk.core.model

import io.hackle.sdk.common.User

/**
 * @author Yong
 */
sealed class Experiment {

    abstract val id: Long
    abstract val key: Long
    abstract val type: Type
    abstract val variations: Map<Long, Variation>
    abstract val overrides: Map<String, Long>

    class Draft(
        override val id: Long,
        override val key: Long,
        override val type: Type,
        override val variations: Map<Long, Variation>,
        override val overrides: Map<String, Long>,
    ) : Experiment()

    class Running(
        override val id: Long,
        override val key: Long,
        override val type: Type,
        override val variations: Map<Long, Variation>,
        override val overrides: Map<String, Long>,
        val bucket: Bucket
    ) : Experiment()

    class Paused(
        override val id: Long,
        override val key: Long,
        override val type: Type,
        override val variations: Map<Long, Variation>,
        override val overrides: Map<String, Long>,
    ) : Experiment()


    class Completed(
        override val id: Long,
        override val key: Long,
        override val type: Type,
        override val variations: Map<Long, Variation>,
        override val overrides: Map<String, Long>,
        private val winnerVariationId: Long
    ) : Experiment() {
        val winnerVariation: Variation get() = variations.getValue(winnerVariationId)
    }

    fun getVariationOrNull(variationId: Long): Variation? {
        return variations[variationId]
    }

    fun getOverriddenVariationOrNull(user: User): Variation? {
        val overriddenVariationId = overrides[user.id] ?: return null
        val overriddenVariation = getVariationOrNull(overriddenVariationId)
        return requireNotNull(overriddenVariation) { "experiment[$id] variation[$overriddenVariationId]" }
    }

    enum class Type {
        AB_TEST, FEATURE_FLAG
    }
}
