package io.hackle.sdk.core.evaluation.action

import io.hackle.sdk.common.User
import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.model.Action
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Variation
import io.hackle.sdk.core.workspace.Workspace

internal class ActionResolver(
    private val bucketer: Bucketer
) {

    fun resolveOrNull(action: Action, workspace: Workspace, experiment: Experiment, user: User): Variation? {
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
        user: User
    ): Variation? {
        val bucket = requireNotNull(workspace.getBucketOrNull(action.bucketId)) { "bucket[${action.bucketId}]" }
        val allocatedSlot = bucketer.bucketing(bucket, user) ?: return null
        return experiment.getVariationOrNull(allocatedSlot.variationId)
    }
}
