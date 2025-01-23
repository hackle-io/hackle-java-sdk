package io.hackle.sdk.core.evaluation.match

import com.google.gson.Gson
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.NUMBER_OF_EVENTS_IN_DAYS
import io.hackle.sdk.core.model.TargetEvent
import io.hackle.sdk.core.model.TargetSegmentationExpression
import io.hackle.sdk.core.model.TargetSegmentationOption

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

/**
 * TargetSegmentationExpressionMatcher
 */
internal abstract class TargetSegmentationExpressionMatcher {
    protected abstract val valueOperatorMatcher: ValueOperatorMatcher
    internal val gson = Gson()

    fun matches(request: Evaluator.Request, condition: Target.Condition): Boolean {
        requireNotNull(condition.key.name)
        return matches(request.user.targetEvents, condition)
    }

    protected abstract fun matches(targetEvents: List<TargetEvent>, condition: Target.Condition): Boolean
}

/**
 * NumberOfEventsInDaysMatcher
 *
 * 기간 내 이벤트 발생 횟수
 */
internal class NumberOfEventsInDaysMatcher(
    override val valueOperatorMatcher: ValueOperatorMatcher
): TargetSegmentationExpressionMatcher() {

    override fun matches(targetEvents: List<TargetEvent>, condition: Target.Condition): Boolean {
        val numberOfEventsInDays = condition.key.toNumberOfEventsInDays()
        val daysAgoUtc = Clock.SYSTEM.currentMillis() - Clock.daysToMillis(numberOfEventsInDays.timeRange.periodDays)
        val filteredTargetEvents = targetEvents
            .filter { it.eventKey == numberOfEventsInDays.eventKey }

        return match(filteredTargetEvents, daysAgoUtc, condition, numberOfEventsInDays.filters)
    }

    private fun match(
        filteredTargetEvents: List<TargetEvent>,
        daysAgoUtc: Long,
        condition: Target.Condition,
        filters: List<TargetSegmentationOption.PropertyFilter>? = null
    ): Boolean {
        val targetEventMap = filteredTargetEvents
            .groupBy { it.property?.key }
        return if (filters.isNullOrEmpty()) {
            val numOfEvents = eventCounts(targetEventMap[null], daysAgoUtc)
            valueOperatorMatcher.matches(numOfEvents, condition.match)
        } else {
            filters.all { propertyFilter ->
                val numOfEvents = eventCountsWithProperty(targetEventMap[propertyFilter.propertyKey.name], propertyFilter, daysAgoUtc)
                valueOperatorMatcher.matches(numOfEvents, condition.match)
            }
        }
    }

    private fun eventCounts(events: List<TargetEvent>?, daysAgoUtc: Long): Int {
        return events?.firstOrNull()?.let {
            it.stats.filter { stat -> stat.date >= daysAgoUtc }.sumOf { stat -> stat.count }
        } ?: 0
    }

    private fun eventCountsWithProperty(events: List<TargetEvent>?, propertyFilter: TargetSegmentationOption.PropertyFilter, daysAgoUtc: Long): Int {
        return events?.firstOrNull { event ->
            event.property?.let { valueOperatorMatcher.matches(it.value, propertyFilter.match) } == true
        }?.let {
            it.stats.filter { stat -> stat.date >= daysAgoUtc }.sumOf { stat -> stat.count }
        } ?: 0
    }

    private fun Target.Key.toNumberOfEventsInDays(): TargetSegmentationExpression.NumberOfEventsInDays {
        require(type == NUMBER_OF_EVENTS_IN_DAYS) { "Unsupported Target.Key.Type [${type}]" }
        return gson.fromJson(name, TargetSegmentationExpression.NumberOfEventsInDays::class.java)
    }
}
