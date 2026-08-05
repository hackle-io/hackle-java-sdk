package io.hackle.sdk.core.evaluation.service.experiment.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.evaluation.service.experiment.mode.local.ExperimentLocalEvaluateRequest
import io.hackle.sdk.core.model.TargetRule

internal class ExperimentTargetRuleDeterminer(
    private val targetMatcher: TargetMatcher,
) {
    fun determineTargetRuleOrNull(request: ExperimentLocalEvaluateRequest, context: Evaluator.Context): TargetRule? {
        return request.experiment.targetRules.find { targetMatcher.matches(request, context, it.target) }
    }
}
