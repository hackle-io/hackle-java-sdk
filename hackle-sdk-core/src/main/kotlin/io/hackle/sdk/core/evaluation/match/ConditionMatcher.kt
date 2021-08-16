package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.common.User
import io.hackle.sdk.core.model.Segment
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.*
import io.hackle.sdk.core.workspace.Workspace

interface ConditionMatcher {
    fun matches(condition: Target.Condition, workspace: Workspace, user: User): Boolean
}

internal class SegmentConditionMatcher(
    private val segmentMatcher: SegmentMatcher,
) : ConditionMatcher {
    override fun matches(condition: Target.Condition, workspace: Workspace, user: User): Boolean {
        require(condition.key.type == SEGMENT) { "Condition key type must be SEGMENT" }
        return condition.match.values.asSequence()
            .filterIsInstance<Number>()
            .mapNotNull { workspace.getSegmentOrNull(it.toLong()) }
            .any { segmentMatcher.matches(it, workspace, user) }
    }
}

internal class SegmentMatcher(
    private val propertyConditionMatcher: PropertyConditionMatcher,
) {
    fun matches(segment: Segment, workspace: Workspace, user: User): Boolean {
        return segment.target.conditions.asSequence()
            .filter { it.key.type != SEGMENT }
            .any { propertyConditionMatcher.matches(it, workspace, user) }
    }
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
            SEGMENT -> throw IllegalArgumentException("Unsupported type [${key.type}]")
            HACKLE_PROPERTY -> null
            USER_PROPERTY -> user.properties[key.name]
        }
    }
}

class ConditionMatcherFactory {

    private val propertyConditionMatcher: ConditionMatcher
    private val segmentConditionMatcher: ConditionMatcher

    init {
        this.propertyConditionMatcher = PropertyConditionMatcher(ValueOperatorMatcher(ValueOperatorMatcherFactory()))
        this.segmentConditionMatcher = SegmentConditionMatcher(SegmentMatcher(this.propertyConditionMatcher))
    }

    fun getMatcher(type: Target.Key.Type): ConditionMatcher {
        return when (type) {
            USER_PROPERTY, HACKLE_PROPERTY -> propertyConditionMatcher
            SEGMENT -> segmentConditionMatcher
        }
    }
}
