package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentRequest
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.TargetRule

/**
 * @author Yong
 */
internal class ExperimentTargetRuleDeterminer(
    private val targetMatcher: TargetMatcher
) {

    fun determineTargetRuleOrNull(request: ExperimentRequest, context: Evaluator.Context): TargetRule? {
        return request.experiment.targetRules.find { targetMatcher.matches(request, context, it.target) }
    }
}
