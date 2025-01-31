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
    companion object {
        const val DEFAULT_PROPERTY_KEY = "DEFAULT_HACKLE_PROPERTY"
    }

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
        filters: List<TargetSegmentationOption.PropertyFilter>?
    ): Boolean {
        // 프로퍼티 키가 없으면 null 대신 기본 키로 설정
        val targetEventMap = filteredTargetEvents
            .groupBy { it.property?.key ?: DEFAULT_PROPERTY_KEY }

        // 필터가 있으면 모든 필터를 만족해야 함
        return filters?.all { propertyFilter ->
            // 필터의 프로퍼티 키에 해당하는 이벤트가 있는지 확인
            targetEventMap[propertyFilter.propertyKey.name]?.any { event ->
                event.property?.let { property ->
                    val eventCount = event.countWithinDays(daysAgoUtc)
                    valueOperatorMatcher.matches(property.value, propertyFilter.match) &&
                            valueOperatorMatcher.matches(eventCount, condition.match)
                } ?: false
            } ?: false
        }
            // 필터가 없으면 프로퍼티 없는 경우
            // 즉, 기본 키로 설정된 이벤트만 확인
            ?: (targetEventMap[DEFAULT_PROPERTY_KEY]?.any {
                val eventCount = it.countWithinDays(daysAgoUtc)
                valueOperatorMatcher.matches(eventCount, condition.match)
            } ?: false)
    }

    /**
     * 기간 내 이벤트 발생 횟수
     */
    private val TargetEvent.countWithinDays: (Long) -> Int
        get() = { daysAgoUtc ->
            stats.filter { it.date >= daysAgoUtc }.sumOf { it.count }
    }

    /**
     * Target.Key to NumberOfEventsInDays
     */
    private fun Target.Key.toNumberOfEventsInDays(): TargetSegmentationExpression.NumberOfEventsInDays {
        require(type == NUMBER_OF_EVENTS_IN_DAYS) { "Unsupported Target.Key.Type [${type}]" }
        return gson.fromJson(name, TargetSegmentationExpression.NumberOfEventsInDays::class.java)
    }
}
