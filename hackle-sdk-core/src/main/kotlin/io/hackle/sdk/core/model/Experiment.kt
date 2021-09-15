package io.hackle.sdk.core.model

import io.hackle.sdk.common.User

/**
 * @author Yong
 */
sealed class Experiment {

    abstract val id: Long
    abstract val key: Long
    abstract val type: Type
    abstract val variations: List<Variation>
    abstract val overrides: Map<String, Long>

    data class Draft(
        override val id: Long,
        override val key: Long,
        override val type: Type,
        override val variations: List<Variation>,
        override val overrides: Map<String, Long>,
    ) : Experiment()

    data class Running(
        override val id: Long,
        override val key: Long,
        override val type: Type,
        override val variations: List<Variation>,
        override val overrides: Map<String, Long>,
        val targetAudiences: List<Target>,
        val targetRules: List<TargetRule>,
        val defaultRule: Action
    ) : Experiment()

    data class Paused(
        override val id: Long,
        override val key: Long,
        override val type: Type,
        override val variations: List<Variation>,
        override val overrides: Map<String, Long>,
    ) : Experiment()


    data class Completed(
        override val id: Long,
        override val key: Long,
        override val type: Type,
        override val variations: List<Variation>,
        override val overrides: Map<String, Long>,
        private val winnerVariationId: Long
    ) : Experiment() {
        val winnerVariation: Variation get() = requireNotNull(getVariationOrNull(winnerVariationId)) { "variation[$winnerVariationId]" }
    }

    internal fun getVariationOrNull(variationId: Long): Variation? {
        return variations.find { it.id == variationId }
    }

    internal fun getVariationOrNull(variationKey: String): Variation? {
        return variations.find { it.key == variationKey }
    }

    internal fun getOverriddenVariationOrNull(user: User): Variation? {
        val overriddenVariationId = overrides[user.id] ?: return null
        val overriddenVariation = getVariationOrNull(overriddenVariationId)
        return requireNotNull(overriddenVariation) { "experiment[$id] variation[$overriddenVariationId]" }
    }

    enum class Type {
        AB_TEST, FEATURE_FLAG
    }
}
