package io.hackle.sdk.core.evaluation.service.experiment.match

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.service.experiment.mode.local.ExperimentLocalEvaluateRequest
import io.hackle.sdk.core.model.Action
import io.hackle.sdk.core.model.Variation

internal class ExperimentActionResolver(
    private val bucketer: Bucketer,
) {

    fun resolveOrNull(request: ExperimentLocalEvaluateRequest, action: Action): Variation? {
        return when (action) {
            is Action.Variation -> resolveVariation(request, action)
            is Action.Bucket -> resolveBucket(request, action)
        }
    }

    private fun resolveVariation(request: ExperimentLocalEvaluateRequest, action: Action.Variation): Variation {
        return requireNotNull(request.experiment.getVariationOrNull(action.variationId)) { "variation[${action.variationId}]" }
    }

    private fun resolveBucket(
        request: ExperimentLocalEvaluateRequest,
        action: Action.Bucket,
    ): Variation? {
        val bucket = requireNotNull(request.workspace.getBucketOrNull(action.bucketId)) { "bucket[${action.bucketId}]" }
        val identifier = request.user.identifiers[request.experiment.identifierType] ?: return null
        val allocatedSlot = bucketer.bucketing(bucket, identifier) ?: return null
        return request.experiment.getVariationOrNull(allocatedSlot.variationId)
    }
}
