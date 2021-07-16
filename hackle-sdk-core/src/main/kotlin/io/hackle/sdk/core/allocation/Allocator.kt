package io.hackle.sdk.core.allocation

import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.allocation.Allocation.*
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Experiment.Completed
import io.hackle.sdk.core.model.FeatureFlag

/**
 * @author Yong
 */
internal class Allocator(
    private val bucketer: Bucketer
) {


    fun allocate(experiment: Experiment, user: User): Allocation {

        val overriddenVariation = experiment.getOverriddenVariationOrNull(user)
        if (overriddenVariation != null) {
            return Allocated(OVERRIDDEN, overriddenVariation)
        }

        return when (experiment) {
            is Experiment.Ready -> NotAllocated(TRAFFIC_NOT_ALLOCATED)
            is Experiment.Running -> allocate(experiment, user)
            is Experiment.Paused -> NotAllocated(TRAFFIC_NOT_ALLOCATED)
            is Completed -> Allocated(EXPERIMENT_COMPLETED, experiment.winnerVariation)
        }
    }


    fun allocate(featureFlag: FeatureFlag, user: User): Allocation {

        val overriddenVariation = featureFlag.getOverriddenVariationOrNull(user)
        if (overriddenVariation != null) {
            return Allocated(OVERRIDDEN, overriddenVariation)
        }

        val allocatedSlot = bucketer.bucketing(featureFlag.bucket, user) ?: return NotAllocated(TRAFFIC_NOT_ALLOCATED)
        val allocatedVariation =
            featureFlag.getVariationOrNull(allocatedSlot.variationId) ?: return NotAllocated(TRAFFIC_NOT_ALLOCATED)

        return Allocated(TRAFFIC_ALLOCATED, allocatedVariation)
    }


    private fun allocate(runningExperiment: Experiment.Running, user: User): Allocation {

        val allocatedSlot =
            bucketer.bucketing(runningExperiment.bucket, user) ?: return NotAllocated(TRAFFIC_NOT_ALLOCATED)
        val allocatedVariation = runningExperiment.getVariationOrNull(allocatedSlot.variationId) ?: return NotAllocated(TRAFFIC_NOT_ALLOCATED)

        if (allocatedVariation.isDropped) {
            return NotAllocated(VARIATION_DROPPED)
        }

        return Allocated(TRAFFIC_ALLOCATED, allocatedVariation)
    }
}
