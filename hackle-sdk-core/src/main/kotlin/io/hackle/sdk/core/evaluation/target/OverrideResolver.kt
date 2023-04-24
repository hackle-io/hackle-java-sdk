package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentRequest
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.Variation

internal class OverrideResolver(
    private val manualOverrideStorage: ManualOverrideStorage,
    private val targetMatcher: TargetMatcher,
    private val actionResolver: ActionResolver
) {

    fun resolveOrNull(request: ExperimentRequest, context: Evaluator.Context): Variation? {
        return resolveManualOverride(request)
            ?: resolveUserOverride(request)
            ?: resolveSegmentOverride(request, context)
    }

    private fun resolveManualOverride(request: ExperimentRequest): Variation? {
        return manualOverrideStorage[request.experiment, request.user]
    }

    private fun resolveUserOverride(request: ExperimentRequest): Variation? {
        val experiment = request.experiment
        val identifier = request.user.identifiers[experiment.identifierType] ?: return null
        val overriddenVariationId = experiment.userOverrides[identifier] ?: return null
        return experiment.getVariationOrNull(overriddenVariationId)
    }

    private fun resolveSegmentOverride(request: ExperimentRequest, context: Evaluator.Context): Variation? {
        val experiment = request.experiment
        val overriddenRule =
            experiment.segmentOverrides.find { targetMatcher.matches(request, context, it.target) } ?: return null
        return actionResolver.resolveOrNull(request, overriddenRule.action)
    }
}
