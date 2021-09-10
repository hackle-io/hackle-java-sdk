package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.common.User
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.workspace.Workspace

internal class TargetMatcher(
    private val conditionMatcherFactory: ConditionMatcherFactory
) {

    fun matches(target: Target, workspace: Workspace, user: User): Boolean {
        return target.conditions.all { matches(it, workspace, user) }
    }

    private fun matches(condition: Target.Condition, workspace: Workspace, user: User): Boolean {
        val conditionMatcher = conditionMatcherFactory.getMatcher(condition.key.type)
        return conditionMatcher.matches(condition, workspace, user)
    }
}
