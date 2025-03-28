package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.COHORT

internal class CohortConditionMatcher(
    private val valueOperatorMatcher: ValueOperatorMatcher,
) : ConditionMatcher {
    override fun matches(request: Evaluator.Request, context: Evaluator.Context, condition: Target.Condition): Boolean {
        require(condition.key.type == COHORT) { "Unsupported Target.Key.Type [${condition.key.type}]" }
        val cohortIds = request.user.cohorts.map { it.id }
        return valueOperatorMatcher.matches(cohortIds, condition.match)
    }
}
