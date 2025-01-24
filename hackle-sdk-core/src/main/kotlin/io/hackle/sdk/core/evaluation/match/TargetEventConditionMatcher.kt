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
 */
internal class TargetEventConditionMatcher(
    private val numberOfEventsInDaysMatcher: NumberOfEventsInDaysMatcher
): ConditionMatcher {
    override fun matches(request: Evaluator.Request, context: Evaluator.Context, condition: Target.Condition): Boolean {
        return when (condition.key.type) {
            NUMBER_OF_EVENTS_IN_DAYS -> numberOfEventsInDaysMatcher.matches(request.user.targetEvents, condition)
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

    internal abstract fun matches(targetEvents: List<TargetEvent>, condition: Target.Condition): Boolean
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

    /**
     * 조건에 맞는지 확인
     * @param filteredTargetEvents eventKey 로 필터링된 TargetEvent 목록
     * @param daysAgoUtc 확인 기간의 시작일 UTC timestamp
     * @param condition 조건
     * @param filters 프로퍼티 필터
     * @return 조건에 맞는지 여부
     */
    private fun match(
        filteredTargetEvents: List<TargetEvent>,
        daysAgoUtc: Long,
        condition: Target.Condition,
        filters: List<TargetSegmentationOption.PropertyFilter>? = null
    ): Boolean {
        val targetEventMap = filteredTargetEvents
            .groupBy { it.property?.key }
        return if (filters.isNullOrEmpty()) {
            val numOfEvents = eventCountsWithoutProperty(targetEventMap[null], daysAgoUtc)
            valueOperatorMatcher.matches(numOfEvents, condition.match)
        } else {
            filters.all { propertyFilter ->
                val numOfEvents = eventCountsWithProperty(targetEventMap[propertyFilter.propertyKey.name], propertyFilter, daysAgoUtc)
                valueOperatorMatcher.matches(numOfEvents, condition.match)
            }
        }
    }

    /**
     * 프로퍼티가 없는 경우 기간 내 이벤트 발생 횟수
     */
    private fun eventCountsWithoutProperty(events: List<TargetEvent>?, daysAgoUtc: Long): Int {
        return events?.firstOrNull()?.let {
            it.stats.filter { stat -> stat.date >= daysAgoUtc }.sumOf { stat -> stat.count }
        } ?: 0
    }

    /**
     *  프로퍼티가 있는 경우 기간 내 이벤트 발생 횟수
     */
    private fun eventCountsWithProperty(events: List<TargetEvent>?, propertyFilter: TargetSegmentationOption.PropertyFilter, daysAgoUtc: Long): Int {
        return events?.firstOrNull { event ->
            event.property?.let { valueOperatorMatcher.matches(it.value, propertyFilter.match) } == true
        }?.let {
            it.stats.filter { stat -> stat.date >= daysAgoUtc }.sumOf { stat -> stat.count }
        } ?: 0
    }

    /**
     * Target.Key to NumberOfEventsInDays
     */
    private fun Target.Key.toNumberOfEventsInDays(): TargetSegmentationExpression.NumberOfEventsInDays {
        require(type == NUMBER_OF_EVENTS_IN_DAYS) { "Unsupported Target.Key.Type [${type}]" }
        return gson.fromJson(name, TargetSegmentationExpression.NumberOfEventsInDays::class.java)
    }
}
