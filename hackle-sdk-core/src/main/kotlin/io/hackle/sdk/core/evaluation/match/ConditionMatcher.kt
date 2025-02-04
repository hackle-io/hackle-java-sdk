package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.*

internal interface ConditionMatcher {
    fun matches(
        request: Evaluator.Request,
        context: Evaluator.Context,
        condition: Target.Condition
    ): Boolean
}

internal class ConditionMatcherFactory(evaluator: Evaluator, clock: Clock) {

    private val userConditionMatcher: ConditionMatcher
    private val segmentConditionMatcher: ConditionMatcher
    private val experimentConditionMatcher: ConditionMatcher
    private val eventConditionMatcher: ConditionMatcher
    private val cohortConditionMatcher: CohortConditionMatcher
    private val targetEventConditionMatcher: TargetEventConditionMatcher

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

        this.cohortConditionMatcher = CohortConditionMatcher(valueOperatorMatcher)
        this.targetEventConditionMatcher = TargetEventConditionMatcher(
            NumberOfEventsInDaysMatcher(valueOperatorMatcher, clock),
            NumberOfEventsWithPropertyInDaysMatcher(valueOperatorMatcher, clock)
        )
    }

    fun getMatcher(type: Target.Key.Type): ConditionMatcher {
        return when (type) {
            USER_ID, USER_PROPERTY, HACKLE_PROPERTY -> userConditionMatcher
            SEGMENT -> segmentConditionMatcher
            AB_TEST, FEATURE_FLAG -> experimentConditionMatcher
            EVENT_PROPERTY -> eventConditionMatcher
            COHORT -> cohortConditionMatcher
            NUMBER_OF_EVENTS_IN_DAYS, NUMBER_OF_EVENTS_WITH_PROPERTY_IN_DAYS -> targetEventConditionMatcher
        }
    }
}
