package io.hackle.sdk.core.evaluation.service.experiment.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.evaluation.service.experiment.mode.local.ExperimentLocalEvaluateRequest

internal class ExperimentTargetDeterminer(
    private val targetMatcher: TargetMatcher,
) {
    fun isUserInExperimentTarget(request: ExperimentLocalEvaluateRequest, context: Evaluator.Context): Boolean {
        if (request.experiment.targetAudiences.isEmpty()) {
            return true
        }
        return request.experiment.targetAudiences.any { targetMatcher.matches(request, context, it) }
    }
}
