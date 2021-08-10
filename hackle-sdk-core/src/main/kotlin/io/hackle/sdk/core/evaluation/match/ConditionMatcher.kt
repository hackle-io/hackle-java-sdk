package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.common.User
import io.hackle.sdk.core.model.Segment
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.*
import io.hackle.sdk.core.workspace.Workspace

interface ConditionMatcher {
    fun matches(condition: Target.Condition, workspace: Workspace, user: User): Boolean
}

internal class SegmentMatcher(
    private val propertyMatcher: PropertyMatcher,
) : ConditionMatcher {
    override fun matches(condition: Target.Condition, workspace: Workspace, user: User): Boolean {
        require(condition.key.type == SEGMENT)
        return condition.match.values.asSequence()
            .filterIsInstance<Long>()
            .mapNotNull { workspace.getSegmentOrNull(it) }
            .any { segmentMatches(it, workspace, user) }
    }

    private fun segmentMatches(segment: Segment, workspace: Workspace, user: User): Boolean {
        return segment.target.conditions.asSequence()
            .filter { it.key.type != SEGMENT }
            .any { propertyMatcher.matches(it, workspace, user) }
    }
}

internal class PropertyMatcher(
    private val valueOperatorMatcher: ValueOperatorMatcher
) : ConditionMatcher {
    override fun matches(condition: Target.Condition, workspace: Workspace, user: User): Boolean {
        val userValue = resolveProperty(user, condition.key) ?: return false
        return valueOperatorMatcher.matches(userValue, condition.match)
    }

    private fun resolveProperty(user: User, key: Target.Key): Any? {
        return when (key.type) {
            SEGMENT -> throw IllegalArgumentException("Unsupported type [${key.type}]")
            HACKLE_PROPERTY -> null
            USER_PROPERTY -> user.properties[key.name]
        }
    }
}

internal class ConditionMatcherFactory(
    private val propertyMatcher: ConditionMatcher,
    private val segmentMatcher: ConditionMatcher
) {

    fun getMatcher(type: Target.Key.Type): ConditionMatcher {
        return when (type) {
            USER_PROPERTY, HACKLE_PROPERTY -> propertyMatcher
            SEGMENT -> segmentMatcher
        }
    }
}
