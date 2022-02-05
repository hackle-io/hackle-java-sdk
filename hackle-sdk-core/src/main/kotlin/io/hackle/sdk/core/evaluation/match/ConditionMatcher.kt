package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.HackleUser
import io.hackle.sdk.core.model.Segment
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.*
import io.hackle.sdk.core.workspace.Workspace

interface ConditionMatcher {
    fun matches(condition: Target.Condition, workspace: Workspace, user: HackleUser): Boolean
}

internal class UserConditionMatcher(
    private val userValueResolver: UserValueResolver,
    private val valueOperatorMatcher: ValueOperatorMatcher
) : ConditionMatcher {
    override fun matches(condition: Target.Condition, workspace: Workspace, user: HackleUser): Boolean {
        val userValue = userValueResolver.resolveOrNull(user, condition.key) ?: return false
        return valueOperatorMatcher.matches(userValue, condition.match)
    }
}

internal class UserValueResolver {
    fun resolveOrNull(user: HackleUser, key: Target.Key): Any? {
        return when (key.type) {
            USER_ID -> user.id
            USER_PROPERTY -> user.properties[key.name]
            HACKLE_PROPERTY -> user.hackleProperties[key.name]
            SEGMENT -> throw IllegalArgumentException("Unsupported target.key.type [${key.type}]")
        }
    }
}

internal class SegmentConditionMatcher(
    private val segmentMatcher: SegmentMatcher
) : ConditionMatcher {
    override fun matches(condition: Target.Condition, workspace: Workspace, user: HackleUser): Boolean {
        require(condition.key.type == SEGMENT) { "Unsupported target.key.type [${condition.key.type}]" }
        return condition.match.values.any { matches(it, workspace, user) }
    }

    private fun matches(value: Any, workspace: Workspace, user: HackleUser): Boolean {
        val segmentKey = requireNotNull(value as? String) { "SegmentKey[$value]" }
        val segment = requireNotNull(workspace.getSegmentOrNull(segmentKey)) { "Segment[$segmentKey]" }
        return segmentMatcher.matches(segment, workspace, user)
    }
}

internal class SegmentMatcher(
    private val userConditionMatcher: UserConditionMatcher
) {
    fun matches(segment: Segment, workspace: Workspace, user: HackleUser): Boolean {
        return segment.targets.any { matches(it, workspace, user) }
    }

    private fun matches(target: Target, workspace: Workspace, user: HackleUser): Boolean {
        return target.conditions.all { userConditionMatcher.matches(it, workspace, user) }
    }
}

internal class ConditionMatcherFactory {

    private val userConditionMatcher: ConditionMatcher
    private val segmentConditionMatcher: ConditionMatcher

    init {
        this.userConditionMatcher =
            UserConditionMatcher(UserValueResolver(), ValueOperatorMatcher(ValueOperatorMatcherFactory()))
        this.segmentConditionMatcher = SegmentConditionMatcher(SegmentMatcher(this.userConditionMatcher))
    }

    fun getMatcher(type: Target.Key.Type): ConditionMatcher {
        return when (type) {
            USER_ID, USER_PROPERTY, HACKLE_PROPERTY -> userConditionMatcher
            SEGMENT -> segmentConditionMatcher
        }
    }
}
