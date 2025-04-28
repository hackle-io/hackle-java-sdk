package io.hackle.sdk.common.decision

import io.hackle.sdk.common.HackleExperiment
import io.hackle.sdk.common.ParameterConfig
import io.hackle.sdk.common.Variation

/**
 * An object that contains the decided variation and the reason for the decision.
 */
data class Decision internal constructor(
    val experiment: HackleExperiment?,
    val variation: Variation,
    val reason: DecisionReason,
    val config: ParameterConfig,
) : ParameterConfig by config {

    override fun toString(): String {
        return "ExperimentDecision(experiment=$experiment, variation=$variation, reason=$reason, config=${config.parameters})"
    }

    companion object {

        @JvmStatic
        @JvmOverloads
        fun of(
            variation: Variation,
            reason: DecisionReason,
            config: ParameterConfig = ParameterConfig.empty(),
            experiment: HackleExperiment? = null,
        ): Decision {
            return Decision(experiment, variation, reason, config)
        }
    }
}

/**
 * An object that contains the decided flag and the reason for the feature flag decision.
 */
data class FeatureFlagDecision internal constructor(
    val featureFlag: HackleExperiment?,
    val isOn: Boolean,
    val reason: DecisionReason,
    val config: ParameterConfig,
) : ParameterConfig by config {

    override fun toString(): String {
        return "FeatureFlagDecision(featureFlag=$featureFlag, isOn=$isOn, reason=$reason, config=${config.parameters})"
    }

    companion object {

        @JvmStatic
        @JvmOverloads
        fun on(
            reason: DecisionReason,
            config: ParameterConfig = ParameterConfig.empty(),
            featureFlag: HackleExperiment? = null,
        ): FeatureFlagDecision {
            return FeatureFlagDecision(featureFlag, true, reason, config)
        }

        @JvmStatic
        @JvmOverloads
        fun off(
            reason: DecisionReason,
            config: ParameterConfig = ParameterConfig.empty(),
            featureFlag: HackleExperiment? = null,
        ): FeatureFlagDecision {
            return FeatureFlagDecision(featureFlag, false, reason, config)
        }
    }
}

/**
 * An object that contains the decided remote config parameter value and the reason for the decision.
 */
data class RemoteConfigDecision<T> internal constructor(
    val value: T,
    val reason: DecisionReason
) {

    companion object {

        @JvmStatic
        fun <T> of(value: T, reason: DecisionReason): RemoteConfigDecision<T> {
            return RemoteConfigDecision(value, reason)
        }
    }
}

/**
 * Describes the reason for the [Variation] decision.
 */
enum class DecisionReason {

    SDK_NOT_READY,
    EXCEPTION,
    INVALID_INPUT,

    EXPERIMENT_NOT_FOUND,
    EXPERIMENT_DRAFT,
    EXPERIMENT_PAUSED,
    EXPERIMENT_COMPLETED,
    OVERRIDDEN,
    TRAFFIC_NOT_ALLOCATED,
    NOT_IN_MUTUAL_EXCLUSION_EXPERIMENT,
    IDENTIFIER_NOT_FOUND,
    VARIATION_DROPPED,
    TRAFFIC_ALLOCATED,
    TRAFFIC_ALLOCATED_BY_TARGETING,
    NOT_IN_EXPERIMENT_TARGET,

    FEATURE_FLAG_NOT_FOUND,
    FEATURE_FLAG_INACTIVE,
    INDIVIDUAL_TARGET_MATCH,
    TARGET_RULE_MATCH,
    DEFAULT_RULE,
    REMOTE_CONFIG_PARAMETER_NOT_FOUND,
    TYPE_MISMATCH,

    UNSUPPORTED_PLATFORM,

    IN_APP_MESSAGE_NOT_FOUND,
    IN_APP_MESSAGE_DRAFT,
    IN_APP_MESSAGE_PAUSED,
    IN_APP_MESSAGE_HIDDEN,
    IN_APP_MESSAGE_TARGET,
    NOT_IN_IN_APP_MESSAGE_PERIOD,
    NOT_IN_IN_APP_MESSAGE_TARGET,
    IN_APP_MESSAGE_FREQUENCY_CAPPED,
    IN_APP_MESSAGE_UNEXPOSED_LAYOUT
}
