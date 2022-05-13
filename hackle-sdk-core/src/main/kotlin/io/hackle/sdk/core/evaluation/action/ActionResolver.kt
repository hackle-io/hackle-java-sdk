package io.hackle.sdk.core.evaluation.action

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.model.Action
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.HackleUser
import io.hackle.sdk.core.model.Variation
import io.hackle.sdk.core.workspace.Workspace

/**
 * @author Yong
 */
internal class ActionResolver(
    private val bucketer: Bucketer
) {

    fun resolveOrNull(action: Action, workspace: Workspace, experiment: Experiment, user: HackleUser): Variation? {
        return when (action) {
            is Action.Variation -> resolveVariation(action, experiment)
            is Action.Bucket -> resolveBucket(action, workspace, experiment, user)
        }
    }

    private fun resolveVariation(action: Action.Variation, experiment: Experiment): Variation {
        return requireNotNull(experiment.getVariationOrNull(action.variationId)) { "variation[${action.variationId}]" }
    }

    private fun resolveBucket(
        action: Action.Bucket,
        workspace: Workspace,
        experiment: Experiment,
        user: HackleUser
    ): Variation? {
        val bucket = requireNotNull(workspace.getBucketOrNull(action.bucketId)) { "bucket[${action.bucketId}]" }
        val identifier = user.identifiers[experiment.identifierType] ?: return null
        val allocatedSlot = bucketer.bucketing(bucket, identifier) ?: return null
        return experiment.getVariationOrNull(allocatedSlot.variationId)
    }
}
