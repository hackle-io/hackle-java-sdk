package io.hackle.sdk.core.decision

import io.hackle.sdk.common.User
import io.hackle.sdk.core.decision.Decision.NotAllocated
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Experiment.Completed
import io.hackle.sdk.core.model.Experiment.Running

/**
 * @author Yong
 */
internal class BucketingDecider(private val bucketer: Bucketer) : Decider {

    override fun decide(experiment: Experiment, user: User): Decision {
        return when (experiment) {
            is Completed -> Decision.ForcedAllocated(experiment.winnerVariationKey)
            is Running -> decide(experiment, user)
        }
    }

    private fun decide(runningExperiment: Running, user: User): Decision {

        val overriddenVariation = runningExperiment.getOverriddenVariationOrNull(user)
        if (overriddenVariation != null) {
            return Decision.ForcedAllocated(overriddenVariation.key)
        }

        val allocatedSlot = bucketer.bucketing(runningExperiment.bucket, user) ?: return NotAllocated
        val allocatedVariation = runningExperiment.getVariationOrNull(allocatedSlot.variationId) ?: return NotAllocated

        if (allocatedVariation.isDropped) {
            return NotAllocated
        }

        return Decision.NaturalAllocated(allocatedVariation)
    }
}
