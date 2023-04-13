package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentRequest
import io.hackle.sdk.core.evaluation.match.TargetMatcher

/**
 * @author Yong
 */
internal class ExperimentTargetDeterminer(
    private val targetMatcher: TargetMatcher
) {

    fun isUserInExperimentTarget(request: ExperimentRequest, context: Evaluator.Context): Boolean {
        if (request.experiment.targetAudiences.isEmpty()) {
            return true
        }
        return request.experiment.targetAudiences.any { targetMatcher.matches(request, context, it) }
    }
}
