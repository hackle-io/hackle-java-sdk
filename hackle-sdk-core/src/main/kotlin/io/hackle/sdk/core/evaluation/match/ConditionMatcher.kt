package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.common.User
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.HACKLE_PROPERTY
import io.hackle.sdk.core.model.Target.Key.Type.USER_PROPERTY
import io.hackle.sdk.core.workspace.Workspace

interface ConditionMatcher {
    fun matches(condition: Target.Condition, workspace: Workspace, user: User): Boolean
}

internal class PropertyConditionMatcher(
    private val valueOperatorMatcher: ValueOperatorMatcher
) : ConditionMatcher {
    override fun matches(condition: Target.Condition, workspace: Workspace, user: User): Boolean {
        val userValue = resolveProperty(user, condition.key) ?: return false
        return valueOperatorMatcher.matches(userValue, condition.match)
    }

    private fun resolveProperty(user: User, key: Target.Key): Any? {
        return when (key.type) {
            HACKLE_PROPERTY -> null
            USER_PROPERTY -> user.properties[key.name]
        }
    }
}

internal class ConditionMatcherFactory {

    private val propertyConditionMatcher: ConditionMatcher

    init {
        this.propertyConditionMatcher = PropertyConditionMatcher(ValueOperatorMatcher(ValueOperatorMatcherFactory()))
    }

    fun getMatcher(type: Target.Key.Type): ConditionMatcher {
        return when (type) {
            USER_PROPERTY, HACKLE_PROPERTY -> propertyConditionMatcher
        }
    }
}
