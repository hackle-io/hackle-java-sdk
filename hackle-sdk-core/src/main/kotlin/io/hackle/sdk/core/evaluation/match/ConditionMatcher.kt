package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.Segment
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.*
import io.hackle.sdk.core.user.HackleUser

internal interface ConditionMatcher {

    fun matches(request: Evaluator.Request, context: Evaluator.Context, condition: Target.Condition): Boolean
}

internal class UserConditionMatcher(
    private val userValueResolver: UserValueResolver,
    private val valueOperatorMatcher: ValueOperatorMatcher
) : ConditionMatcher {
    override fun matches(request: Evaluator.Request, context: Evaluator.Context, condition: Target.Condition): Boolean {
        val userValue = userValueResolver.resolveOrNull(request.user, condition.key) ?: return false
        return valueOperatorMatcher.matches(userValue, condition.match)
    }
}

internal class UserValueResolver {
    fun resolveOrNull(user: HackleUser, key: Target.Key): Any? {
        return when (key.type) {
            USER_ID -> user.identifiers[key.name]
            USER_PROPERTY -> user.properties[key.name]
            HACKLE_PROPERTY -> user.hackleProperties[key.name]
            SEGMENT -> throw IllegalArgumentException("Unsupported target.key.type [${key.type}]")
        }
    }
}

internal class SegmentConditionMatcher(
    private val segmentMatcher: SegmentMatcher
) : ConditionMatcher {

    override fun matches(request: Evaluator.Request, context: Evaluator.Context, condition: Target.Condition): Boolean {
        require(condition.key.type == SEGMENT) { "Unsupported target.key.type [${condition.key.type}]" }
        val isMatched = condition.match.values.any { matches(request, context, it) }
        return condition.match.type.matches(isMatched)
    }

    private fun matches(request: Evaluator.Request, context: Evaluator.Context, value: Any): Boolean {
        val segmentKey = requireNotNull(value as? String) { "SegmentKey[$value]" }
        val segment = requireNotNull(request.workspace.getSegmentOrNull(segmentKey)) { "Segment[$segmentKey]" }
        return segmentMatcher.matches(request, context, segment)
    }
}

internal class SegmentMatcher(
    private val userConditionMatcher: UserConditionMatcher
) {
    fun matches(request: Evaluator.Request, context: Evaluator.Context, segment: Segment): Boolean {
        return segment.targets.any { matches(request, context, it) }
    }

    private fun matches(request: Evaluator.Request, context: Evaluator.Context, target: Target): Boolean {
        return target.conditions.all { userConditionMatcher.matches(request, context, it) }
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
