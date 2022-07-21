package io.hackle.sdk.core.evaluation.mutualexclusion

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.model.Bucket
import io.hackle.sdk.core.model.Container
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

internal class ContainerResolver(
    private val bucketer: Bucketer
) {

    fun isUserInContainerGroup(container: Container, bucket: Bucket, experiment: Experiment, user: HackleUser): Boolean {
        val identifier = user.identifiers[experiment.identifierType] ?: return false
        val allocatedSlot = bucketer.bucketing(bucket, identifier) ?: return false
        val containerGroup = container.getGroupOrNull(allocatedSlot.variationId) ?: return false
        return containerGroup.experiments.contains(experiment.id)
    }

}