package io.hackle.sdk.core.evaluation.match

import com.google.gson.Gson
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.NUMBER_OF_EVENTS_IN_DAYS
import io.hackle.sdk.core.model.TargetEvent
import io.hackle.sdk.core.model.TargetSegmentationExpression

/**
 * TargetEventConditionMatcher
 *
 * 실시간 타겟팅
 * TODO: 명칭 결정되면 객체명 수정 필요
 */
internal class TargetEventConditionMatcher(
    private val numberOfEventsInDaysMatcher: NumberOfEventsInDaysMatcher
): ConditionMatcher {
    override fun matches(request: Evaluator.Request, context: Evaluator.Context, condition: Target.Condition): Boolean {
        return when (condition.key.type) {
            NUMBER_OF_EVENTS_IN_DAYS -> numberOfEventsInDaysMatcher.matches(request, condition)
            else -> throw IllegalArgumentException("Unsupported Target.Key.Type [${condition.key.type}]")
        }
    }
}

internal abstract class TargetSegmentationExpressionMatcher {
    protected abstract val valueOperatorMatcher: ValueOperatorMatcher

    fun matches(request: Evaluator.Request, condition: Target.Condition): Boolean {
        requireNotNull(condition.key.name)
        return matches(request.user.targetEvents, condition)
    }

    protected abstract fun matches(targetEvents: List<TargetEvent>, condition: Target.Condition): Boolean
}

internal class NumberOfEventsInDaysMatcher(
    override val valueOperatorMatcher: ValueOperatorMatcher
): TargetSegmentationExpressionMatcher() {
    override fun matches(targetEvents: List<TargetEvent>, condition: Target.Condition): Boolean {
        val numberOfEventsInDays = condition.key.toNumberOfEventsInDays()
        val periodDays = numberOfEventsInDays.timeRange.periodDays
        val daysAgoUtc = Clock.SYSTEM.currentMillis() - Clock.daysToMillis(periodDays)

        val numOfEvents = targetEvents
            .firstOrNull { it.eventKey == numberOfEventsInDays.eventKey && it.property == null }
            .let {
                it ?: return false
            }
            .stats
            .filter { it.date >= daysAgoUtc }
            .sumOf { it.count }

        return valueOperatorMatcher.matches(numOfEvents, condition.match)
    }
}

internal class NumberOfEventsInDaysWithPropertyMatcher(
    override val valueOperatorMatcher: ValueOperatorMatcher
): TargetSegmentationExpressionMatcher() {
    override fun matches(targetEvents: List<TargetEvent>, condition: Target.Condition): Boolean {
        val numberOfEventsInDays = condition.key.toNumberOfEventsInDays()
        requireNotNull(numberOfEventsInDays.filters)
        val periodDays = numberOfEventsInDays.timeRange.periodDays
        val daysAgoUtc = Clock.SYSTEM.currentMillis() - Clock.daysToMillis(periodDays)

        val filteredTargetEvents = targetEvents
            .filter { it.eventKey == numberOfEventsInDays.eventKey }

        val isMatched = numberOfEventsInDays.filters.all { propertyFilter ->
            val numOfEvents = filteredTargetEvents
                .firstOrNull { event -> event.property?.key ==  propertyFilter.propertyKey.name }
                .let { event ->
                    if(event?.property?.let { valueOperatorMatcher.matches(it.value, propertyFilter.match) } == true) {
                        event
                    } else {
                        return false
                    }
                }
                .stats
                .filter { it.date >= daysAgoUtc }
                .sumOf { it.count }

                return valueOperatorMatcher.matches(numOfEvents, condition.match)
            }

        return isMatched
    }
}


private fun Target.Key.toNumberOfEventsInDays(): TargetSegmentationExpression.NumberOfEventsInDays {
    require(type == NUMBER_OF_EVENTS_IN_DAYS) { "Unsupported Target.Key.Type [${type}]" }
    return Gson().fromJson(name, TargetSegmentationExpression.NumberOfEventsInDays::class.java)
}