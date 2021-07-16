package io.hackle.sdk.core.evaluation

import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.allocation.Bucketer
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.FeatureFlag

internal class Evaluator(
    private val bucketer: Bucketer,
) {

    fun evaluate(experiment: Experiment, user: User, defaultVariationKey: String): Evaluation {
        val overriddenVariation = experiment.getOverriddenVariationOrNull(user)
        if (overriddenVariation != null) {
            return Evaluation.Forced(OVERRIDDEN, overriddenVariation.key)
        }

        return when (experiment) {
            is Experiment.Ready -> Evaluation.Default(TRAFFIC_NOT_ALLOCATED, defaultVariationKey)
            is Experiment.Running -> evaluate(experiment, user, defaultVariationKey)
            is Experiment.Paused -> Evaluation.Default(TRAFFIC_NOT_ALLOCATED, defaultVariationKey)
            is Experiment.Completed -> Evaluation.Forced(EXPERIMENT_COMPLETED, experiment.winnerVariation.key)
        }
    }

    private fun evaluate(runningExperiment: Experiment.Running, user: User, defaultVariationKey: String): Evaluation {

        val allocatedSlot = bucketer.bucketing(runningExperiment.bucket, user)
            ?: return Evaluation.Default(TRAFFIC_NOT_ALLOCATED, defaultVariationKey)
        val allocatedVariation = runningExperiment.getVariationOrNull(allocatedSlot.variationId)
            ?: return Evaluation.Default(TRAFFIC_NOT_ALLOCATED, defaultVariationKey)

        if (allocatedVariation.isDropped) {
            return Evaluation.Default(VARIATION_DROPPED, defaultVariationKey)
        }

        return Evaluation.Identified(TRAFFIC_ALLOCATED, allocatedVariation)
    }


    fun evaluate(featureFlag: FeatureFlag, user: User): Evaluation {

        val overriddenVariation = featureFlag.getOverriddenVariationOrNull(user)
        if (overriddenVariation != null) {
            return Evaluation.Forced(OVERRIDDEN, overriddenVariation.key)
        }


        val allocatedSlot =
            bucketer.bucketing(featureFlag.bucket, user) ?: return Evaluation.None(TRAFFIC_NOT_ALLOCATED)
        val allocatedVariation =
            featureFlag.getVariationOrNull(allocatedSlot.variationId) ?: return Evaluation.None(TRAFFIC_NOT_ALLOCATED)

        return Evaluation.Identified(TRAFFIC_ALLOCATED, allocatedVariation)
    }
}
