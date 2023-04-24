package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.Target

internal class TargetMatcher(
    private val conditionMatcherFactory: ConditionMatcherFactory
) {

    fun matches(request: Evaluator.Request, context: Evaluator.Context, target: Target): Boolean {
        return target.conditions.all { matches(request, context, it) }
    }

    private fun matches(request: Evaluator.Request, context: Evaluator.Context, condition: Target.Condition): Boolean {
        val conditionMatcher = conditionMatcherFactory.getMatcher(condition.key.type)
        return conditionMatcher.matches(request, context, condition)
    }
}
