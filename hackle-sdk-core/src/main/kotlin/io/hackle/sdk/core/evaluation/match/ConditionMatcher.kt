package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.*

interface ConditionMatcher {
    fun matches(
        request: Evaluator.Request,
        context: Evaluator.Context,
        condition: Target.Condition
    ): Boolean
}

class ConditionMatcherFactory(evaluator: Evaluator) {

    private val userConditionMatcher: ConditionMatcher
    private val segmentConditionMatcher: ConditionMatcher
    private val experimentConditionMatcher: ConditionMatcher
    private val eventConditionMatcher: ConditionMatcher

    init {
        val valueOperatorMatcher = ValueOperatorMatcher(ValueOperatorMatcherFactory())
        this.userConditionMatcher = UserConditionMatcher(UserValueResolver(), valueOperatorMatcher)
        this.segmentConditionMatcher = SegmentConditionMatcher(SegmentMatcher(this.userConditionMatcher))
        this.experimentConditionMatcher = ExperimentConditionMatcher(
            AbTestConditionMatcher(evaluator, valueOperatorMatcher),
            FeatureFlagConditionMatcher(evaluator, valueOperatorMatcher)
        )
        this.eventConditionMatcher = EventConditionMatcher(
            EventValueResolver(),
            valueOperatorMatcher
        )
    }

    fun getMatcher(type: Target.Key.Type): ConditionMatcher {
        return when (type) {
            USER_ID, USER_PROPERTY, HACKLE_PROPERTY -> userConditionMatcher
            SEGMENT -> segmentConditionMatcher
            AB_TEST, FEATURE_FLAG -> experimentConditionMatcher
            EVENT_PROPERTY -> eventConditionMatcher
        }
    }
}
