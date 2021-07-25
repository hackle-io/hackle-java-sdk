package io.hackle.sdk.core.evaluation

import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.model.Experiment

internal class Evaluator(
    private val bucketer: Bucketer,
) {

    fun evaluate(experiment: Experiment, user: User, defaultVariationKey: String): Evaluation {
        val overriddenVariation = experiment.getOverriddenVariationOrNull(user)
        if (overriddenVariation != null) {
            return Evaluation.of(OVERRIDDEN, overriddenVariation.key)
        }

        return when (experiment) {
            is Experiment.Draft -> Evaluation.of(EXPERIMENT_DRAFT, defaultVariationKey)
            is Experiment.Running -> evaluate(experiment, user, defaultVariationKey)
            is Experiment.Paused -> Evaluation.of(EXPERIMENT_PAUSED, defaultVariationKey)
            is Experiment.Completed -> Evaluation.of(EXPERIMENT_COMPLETED, experiment.winnerVariation.key)
        }
    }

    private fun evaluate(runningExperiment: Experiment.Running, user: User, defaultVariationKey: String): Evaluation {

        val allocatedSlot = bucketer.bucketing(runningExperiment.bucket, user)
            ?: return Evaluation.of(TRAFFIC_NOT_ALLOCATED, defaultVariationKey)
        val allocatedVariation = runningExperiment.getVariationOrNull(allocatedSlot.variationId)
            ?: return Evaluation.of(TRAFFIC_NOT_ALLOCATED, defaultVariationKey)

        if (allocatedVariation.isDropped) {
            return Evaluation.of(VARIATION_DROPPED, defaultVariationKey)
        }

        return Evaluation.of(TRAFFIC_ALLOCATED, allocatedVariation)
    }
}
