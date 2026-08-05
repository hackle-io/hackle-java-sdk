package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.Target

class TargetMatcher internal constructor(
    private val conditionMatcherFactory: ConditionMatcherFactory,
) {

    fun matches(request: EvaluateRequest, context: Evaluator.Context, target: Target): Boolean {
        return target.conditions.all { matches(request, context, it) }
    }

    fun anyMatches(request: EvaluateRequest, context: Evaluator.Context, targets: List<Target>): Boolean {
        if (targets.isEmpty()) {
            return true
        }
        return targets.any { matches(request, context, it) }
    }

    private fun matches(request: EvaluateRequest, context: Evaluator.Context, condition: Target.Condition): Boolean {
        val conditionMatcher = conditionMatcherFactory.getMatcher(condition.key.type)
        return conditionMatcher.matches(request, context, condition)
    }
}
