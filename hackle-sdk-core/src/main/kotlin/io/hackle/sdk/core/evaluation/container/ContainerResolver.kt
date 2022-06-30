package io.hackle.sdk.core.evaluation.container

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.HackleUser
import io.hackle.sdk.core.workspace.Workspace

internal class ContainerResolver(
    private val bucketer: Bucketer
) {

    fun resolve(workspace: Workspace, experiment: Experiment, identifier: String): Boolean {
        if (experiment.containerId == null) {
            return true
        }

        val container = workspace.getContainerOrNull(experiment.containerId)
        requireNotNull(container) { "container group not exist. containerId = ${experiment.containerId}" }
        val bucket = workspace.getBucketOrNull(container.bucketId)
        requireNotNull(bucket) { "container group bucket not exist. bucketId = ${container.bucketId}" }

        val allocatedSlot = bucketer.bucketing(bucket, identifier) ?: return false
        val containerGroup = container.findGroup(allocatedSlot.variationId) ?: return false
        return containerGroup.experiments.contains(experiment.id)
    }

}