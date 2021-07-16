package io.hackle.sdk.common.decision

import io.hackle.sdk.common.Variation

/**
 * An object that contains the decided variation and the reason for the decision.
 */
data class Decision internal constructor(
    val variation: Variation,
    val reason: DecisionReason
) {

    companion object {
        private val DECISIONS =
            Variation.values().associateWith { v -> DecisionReason.values().associateWith { r -> Decision(v, r) } }

        @JvmStatic
        fun of(variation: Variation, reason: DecisionReason) =
            DECISIONS[variation]?.get(reason) ?: Decision(variation, reason)
    }
}

/**
 * An object that contains the decided flag and the reason for the feature flag decision.
 */
data class FeatureFlagDecision internal constructor(
    val isOn: Boolean,
    val reason: DecisionReason
) {

    companion object {

        private val ON = DecisionReason.values().associate { it to FeatureFlagDecision(true, it) }
        private val OFF = DecisionReason.values().associate { it to FeatureFlagDecision(false, it) }

        @JvmStatic
        fun on(reason: DecisionReason): FeatureFlagDecision {
            return ON[reason] ?: FeatureFlagDecision(true, reason)
        }

        @JvmStatic
        fun off(reason: DecisionReason): FeatureFlagDecision {
            return OFF[reason] ?: FeatureFlagDecision(false, reason)
        }
    }
}

/**
 * Describes the reason for the [Variation] decision.
 */
enum class DecisionReason {

    /**
     * Indicates that the sdk is not ready to use. e.g. invalid SDK key.
     * Returns the default variation.
     */
    SDK_NOT_READY,

    /**
     * Indicates that no experiment was found for the experiment key provided by the caller.
     * Returns the default variation.
     */
    EXPERIMENT_NOT_FOUND,

    /**
     * Indicates that no feature flag was found for the feature flag key provided by the caller.
     */
    FEATURE_FLAG_NOT_FOUND,

    /**
     * Indicates that the experiment was completed.
     * Returns the winner variation.
     */
    EXPERIMENT_COMPLETED,

    /**
     * Indicates that the user has been overridden as a specific variation.
     * Returns the overridden variation.
     */
    OVERRIDDEN,

    /**
     * Indicates that the experiment is running but the user is not allocated to the experiment.
     * Returns the default variation.
     */
    TRAFFIC_NOT_ALLOCATED,

    /**
     * Indicates that the original decided variation has been dropped.
     * Returns the default variation.
     */
    VARIATION_DROPPED,

    /**
     * Indicates that the user has been allocated to the experiment.
     * Returns the allocated variation.
     */
    TRAFFIC_ALLOCATED,

    /**
     * Indicates that the variation could not be decided due to an unexpected exception.
     * Returns the default variation.
     */
    EXCEPTION,
}
