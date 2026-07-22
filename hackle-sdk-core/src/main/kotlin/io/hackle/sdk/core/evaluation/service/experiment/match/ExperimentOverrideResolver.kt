package io.hackle.sdk.core.evaluation.service.experiment.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.evaluation.service.experiment.mode.local.ExperimentLocalEvaluateRequest
import io.hackle.sdk.core.model.Variation

internal class ExperimentOverrideResolver(
    private val manualOverrideStorage: ExperimentManualOverrideStorage,
    private val targetMatcher: TargetMatcher,
    private val actionResolver: ExperimentActionResolver,
) {

    fun resolveOrNull(request: ExperimentLocalEvaluateRequest, context: Evaluator.Context): Variation? {
        return resolveManualOverride(request)
            ?: resolveUserOverride(request)
            ?: resolveSegmentOverride(request, context)
    }

    private fun resolveManualOverride(request: ExperimentLocalEvaluateRequest): Variation? {
        return manualOverrideStorage[request.entity, request.user]
    }

    private fun resolveUserOverride(request: ExperimentLocalEvaluateRequest): Variation? {
        val experiment = request.entity
        val identifier = request.user.identifiers[experiment.identifierType] ?: return null
        val overriddenVariationId = experiment.userOverrides[identifier] ?: return null
        return experiment.getVariationOrNull(overriddenVariationId)
    }

    private fun resolveSegmentOverride(
        request: ExperimentLocalEvaluateRequest,
        context: Evaluator.Context,
    ): Variation? {
        val experiment = request.experiment
        val overriddenRule =
            experiment.segmentOverrides.find { targetMatcher.matches(request, context, it.target) } ?: return null
        return actionResolver.resolveOrNull(request, overriddenRule.action)
    }
}
