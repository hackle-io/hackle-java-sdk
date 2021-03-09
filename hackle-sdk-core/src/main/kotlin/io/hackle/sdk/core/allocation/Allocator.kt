package io.hackle.sdk.core.allocation

import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.allocation.Allocation.*
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Experiment.Completed

/**
 * @author Yong
 */
internal class Allocator(
    private val bucketer: Bucketer
) {

    fun allocate(experiment: Experiment, user: User): Allocation {
        return when (experiment) {
            is Completed -> ForcedAllocated(EXPERIMENT_COMPLETED, experiment.winnerVariationKey)
            is Experiment.Running -> allocate(experiment, user)
        }
    }

    private fun allocate(runningExperiment: Experiment.Running, user: User): Allocation {
        val overriddenVariation = runningExperiment.getOverriddenVariationOrNull(user)
        if (overriddenVariation != null) {
            return ForcedAllocated(OVERRIDDEN, overriddenVariation.key)
        }

        val allocatedSlot = bucketer.bucketing(runningExperiment.bucket, user) ?: return NotAllocated(TRAFFIC_NOT_ALLOCATED)
        val allocatedVariation = runningExperiment.getVariationOrNull(allocatedSlot.variationId) ?: return NotAllocated(TRAFFIC_NOT_ALLOCATED)

        if (allocatedVariation.isDropped) {
            return NotAllocated(VARIATION_DROPPED)
        }

        return Allocated(TRAFFIC_ALLOCATED, allocatedVariation)
    }
}
