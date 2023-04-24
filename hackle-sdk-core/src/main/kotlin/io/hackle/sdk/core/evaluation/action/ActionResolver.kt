package io.hackle.sdk.core.evaluation.action

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentRequest
import io.hackle.sdk.core.model.Action
import io.hackle.sdk.core.model.Variation

/**
 * @author Yong
 */
internal class ActionResolver(
    private val bucketer: Bucketer
) {

    fun resolveOrNull(request: ExperimentRequest, action: Action): Variation? {
        return when (action) {
            is Action.Variation -> resolveVariation(request, action)
            is Action.Bucket -> resolveBucket(request, action)
        }
    }

    private fun resolveVariation(request: ExperimentRequest, action: Action.Variation): Variation {
        return requireNotNull(request.experiment.getVariationOrNull(action.variationId)) { "variation[${action.variationId}]" }
    }

    private fun resolveBucket(
        request: ExperimentRequest,
        action: Action.Bucket
    ): Variation? {
        val bucket = requireNotNull(request.workspace.getBucketOrNull(action.bucketId)) { "bucket[${action.bucketId}]" }
        val identifier = request.user.identifiers[request.experiment.identifierType] ?: return null
        val allocatedSlot = bucketer.bucketing(bucket, identifier) ?: return null
        return request.experiment.getVariationOrNull(allocatedSlot.variationId)
    }
}
