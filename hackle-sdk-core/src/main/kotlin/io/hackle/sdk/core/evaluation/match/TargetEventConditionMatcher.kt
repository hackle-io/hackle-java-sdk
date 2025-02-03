package io.hackle.sdk.core.evaluation.match

import com.google.gson.Gson
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.NUMBER_OF_EVENTS_IN_DAYS
import io.hackle.sdk.core.model.Target.Key.Type.NUMBER_OF_EVENTS_WITH_PROPERTY_IN_DAYS
import io.hackle.sdk.core.model.TargetEvent
import java.util.concurrent.TimeUnit

/**
 * TargetEventConditionMatcher
 */
internal class TargetEventConditionMatcher(
    private val numberOfEventsInDaysMatcher: NumberOfEventsInDaysMatcher,
    private val numberOfEventsWithPropertyInDaysMatcher: NumberOfEventsWithPropertyInDaysMatcher
): ConditionMatcher {
    override fun matches(request: Evaluator.Request, context: Evaluator.Context, condition: Target.Condition): Boolean {
        return when (condition.key.type) {
            NUMBER_OF_EVENTS_IN_DAYS -> numberOfEventsInDaysMatcher.match(request.user.targetEvents, condition)
            NUMBER_OF_EVENTS_WITH_PROPERTY_IN_DAYS -> numberOfEventsWithPropertyInDaysMatcher.match(request.user.targetEvents, condition)
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

    /**
     * TargetEvent List 에서 Target.Condition 에 해당하는 이벤트가 있는지 확인
     */
    internal abstract fun match(targetEvents: List<TargetEvent>, condition: Target.Condition): Boolean
}

/**
 * NumberOfEventsInDaysMatcher
 *
 * 기간 내 이벤트 발생 횟수
 */
internal class NumberOfEventsInDaysMatcher(
    override val valueOperatorMatcher: ValueOperatorMatcher
): TargetSegmentationExpressionMatcher() {

    override fun match(targetEvents: List<TargetEvent>, condition: Target.Condition): Boolean {
        val numberOfEventsInDays = condition.key.toNumberOfEventsInDays()
        val daysAgoUtc = Clock.SYSTEM.currentMillis() - TimeUnit.DAYS.toMillis(numberOfEventsInDays.days.toLong())
        return targetEvents
            .filter { it.eventKey == numberOfEventsInDays.eventKey }
            // 이벤트 키에 프로퍼티 없는 targetEvent 는 1개 이하가 보장
            // 만족하는 이벤트가 하나도 없을 때 null 이벤트를 만들어서 이벤트 횟수 0으로 처리
            .firstOrNull {
                return@firstOrNull it.property == null
            }
            .let { targetEvent ->
                val eventCount = targetEvent.countWithinDays(daysAgoUtc)
                return@let valueOperatorMatcher.matches(eventCount, condition.match)
            }
    }

    /**
     * Target.Key to NumberOfEventsInDays
     */
    private fun Target.Key.toNumberOfEventsInDays(): Target.TargetSegmentationExpression.NumberOfEventsInDays {
        return gson.fromJson(name, Target.TargetSegmentationExpression.NumberOfEventsInDays::class.java)
    }
}

/**
 * NumberOfEventsInDaysWithPropertyMatcher
 *
 * 기간 내 특정 프로퍼티를 가진 이벤트 발생 횟수
 */
internal class NumberOfEventsWithPropertyInDaysMatcher(
    override val valueOperatorMatcher: ValueOperatorMatcher
): TargetSegmentationExpressionMatcher()  {
    override fun match(targetEvents: List<TargetEvent>, condition: Target.Condition): Boolean {
        val numberOfEventsWithPropertyInDays = condition.key.toNumberOfEventsWithPropertyInDays()
        val daysAgoUtc = Clock.SYSTEM.currentMillis() - TimeUnit.DAYS.toMillis(numberOfEventsWithPropertyInDays.days.toLong())

        val filteredTargetEvent = targetEvents
            .filter { it.eventKey == numberOfEventsWithPropertyInDays.eventKey }
            .filter { propertyMatch(it.property, numberOfEventsWithPropertyInDays.propertyFilter) }
            .toMutableList<TargetEvent?>()

        // 만약 만족하는 이벤트의 갯수가 조건의 갯수보다 적다면 null 이벤트를 추가하여 이벤트 횟수 0인 이벤트 추가
        if(filteredTargetEvent.size < numberOfEventsWithPropertyInDays.propertyFilter.match.values.size) {
            filteredTargetEvent.add(null)
        }

        return filteredTargetEvent
            .any { targetEvent ->
                val eventCount = targetEvent.countWithinDays(daysAgoUtc)
                return@any valueOperatorMatcher.matches(eventCount, condition.match)
            }
    }

    /**
     * TargetEvent.Property 와 Target.Condition.Property 비교
     * @param property TargetEvent.Property
     * @param propertyCondition Target.Condition.Property
     * @return Boolean
     */
    private fun propertyMatch(property: TargetEvent.Property?, propertyCondition: Target.Condition): Boolean {
        if (propertyCondition.key.name == property?.key) {
            return valueOperatorMatcher.matches(property.value, propertyCondition.match)
        }
        return false
    }

    /**
     * Target.Key to NumberOfEventsWithPropertyInDays
     */
    private fun Target.Key.toNumberOfEventsWithPropertyInDays(): Target.TargetSegmentationExpression.NumberOfEventsWithPropertyInDays {
        return gson.fromJson(name, Target.TargetSegmentationExpression.NumberOfEventsWithPropertyInDays::class.java)
    }
}

/**
 * 기간 내 이벤트 발생 횟수
 */
private val TargetEvent?.countWithinDays: (Long) -> Int
    get() = { daysAgoUtc ->
        if (this == null) {
            0 // null 이벤트는 이벤트 횟수 0
        } else {
            stats.filter { it.date >= daysAgoUtc }.sumOf { it.count }
        }

    }
