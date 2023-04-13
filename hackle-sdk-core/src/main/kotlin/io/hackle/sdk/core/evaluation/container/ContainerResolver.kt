package io.hackle.sdk.core.evaluation.container

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentRequest
import io.hackle.sdk.core.model.Container
import io.hackle.sdk.core.model.containerGroupId

internal class ContainerResolver(
    private val bucketer: Bucketer
) {
    fun isUserInContainerGroup(request: ExperimentRequest, container: Container): Boolean {
        val identifier = request.user.identifiers[request.experiment.identifierType] ?: return false
        val bucket =
            requireNotNull(request.workspace.getBucketOrNull(container.bucketId)) { "Bucket[${container.bucketId}]" }
        val slot = bucketer.bucketing(bucket, identifier) ?: return false
        val containerGroup =
            requireNotNull(container.getGroupOrNull(slot.containerGroupId)) { "ContainerGroup[${slot.containerGroupId}]" }
        return containerGroup.experiments.contains(request.experiment.id)
    }
}
