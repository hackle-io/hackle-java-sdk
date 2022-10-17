package io.hackle.sdk.common.decision

import io.hackle.sdk.common.ParameterConfig
import io.hackle.sdk.common.Variation

/**
 * An object that contains the decided variation and the reason for the decision.
 */
data class Decision internal constructor(
    val variation: Variation,
    val reason: DecisionReason,
    val config: ParameterConfig,
) : ParameterConfig by config {

    companion object {

        @JvmStatic
        @JvmOverloads
        fun of(
            variation: Variation,
            reason: DecisionReason,
            config: ParameterConfig = ParameterConfig.empty()
        ): Decision {
            return Decision(variation, reason, config)
        }
    }
}

/**
 * An object that contains the decided flag and the reason for the feature flag decision.
 */
data class FeatureFlagDecision internal constructor(
    val isOn: Boolean,
    val reason: DecisionReason,
    val config: ParameterConfig,
) : ParameterConfig by config {

    companion object {

        @JvmStatic
        @JvmOverloads
        fun on(reason: DecisionReason, config: ParameterConfig = ParameterConfig.empty()): FeatureFlagDecision {
            return FeatureFlagDecision(true, reason, config)
        }

        @JvmStatic
        @JvmOverloads
        fun off(reason: DecisionReason, config: ParameterConfig = ParameterConfig.empty()): FeatureFlagDecision {
            return FeatureFlagDecision(false, reason, config)
        }
    }
}

/**
 * Describes the reason for the [Variation] decision.
 */
enum class DecisionReason {

    /**
     * Indicates that the sdk is not ready to use. e.g. invalid SDK key.
     */
    SDK_NOT_READY,

    /**
     * Indicates that the variation could not be decided due to an unexpected exception.
     */
    EXCEPTION,

    /**
     * Indicates that the input value is invalid.
     */
    INVALID_INPUT,

    /**
     * Indicates that no experiment was found for the experiment key provided by the caller.
     */
    EXPERIMENT_NOT_FOUND,

    /**
     * Indicates that the experiment is in draft.
     */
    EXPERIMENT_DRAFT,

    /**
     * Indicates that the experiment was paused.
     */
    EXPERIMENT_PAUSED,

    /**
     * Indicates that the experiment was completed.
     */
    EXPERIMENT_COMPLETED,

    /**
     * Indicates that the user has been overridden as a specific variation.
     */
    OVERRIDDEN,

    /**
     * Indicates that the experiment is running but the user is not allocated to the experiment.
     */
    TRAFFIC_NOT_ALLOCATED,

    /**
     * Indicates that the experiment is running but the user is not allocated to the mutual exclusion experiment.
     */
    NOT_IN_MUTUAL_EXCLUSION_EXPERIMENT,

    /**
     * Indicates that no found identifier of experiment for the user provided by the caller.
     */
    IDENTIFIER_NOT_FOUND,

    /**
     * Indicates that the original decided variation has been dropped.
     */
    VARIATION_DROPPED,

    /**
     * Indicates that the user has been allocated to the experiment.
     */
    TRAFFIC_ALLOCATED,

    /**
     * Indicates that the user is not the target of the experiment.
     */
    NOT_IN_EXPERIMENT_TARGET,

    /**
     * Indicates that no feature flag was found for the feature key provided by the caller.
     */
    FEATURE_FLAG_NOT_FOUND,

    /**
     * Indicates that the feature flag is inactive.
     */
    FEATURE_FLAG_INACTIVE,

    /**
     * Indicates that the user is matched to the individual target of the feature flag.
     */
    INDIVIDUAL_TARGET_MATCH,

    /**
     * Indicates that the user is matched to the target rule of the feature flag.
     */
    TARGET_RULE_MATCH,

    /**
     * Indicates that the user did not match any individual targets or target rules.
     */
    DEFAULT_RULE,

}
