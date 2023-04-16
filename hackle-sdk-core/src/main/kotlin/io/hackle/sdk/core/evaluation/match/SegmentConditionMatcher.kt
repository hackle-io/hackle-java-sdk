package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.Segment
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.SEGMENT

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
