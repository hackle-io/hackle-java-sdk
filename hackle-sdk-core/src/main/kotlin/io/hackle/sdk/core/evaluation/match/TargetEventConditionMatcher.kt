package io.hackle.sdk.core.evaluation.match

import com.google.gson.Gson
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.NUMBER_OF_EVENTS_IN_DAYS
import io.hackle.sdk.core.model.Target.Key.Type.NUMBER_OF_EVENT_WITH_PROPERTY_IN_DAYS
import io.hackle.sdk.core.model.TargetEvent

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
            NUMBER_OF_EVENT_WITH_PROPERTY_IN_DAYS -> numberOfEventsWithPropertyInDaysMatcher.match(request.user.targetEvents, condition)
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
        val daysAgoUtc = Clock.SYSTEM.currentMillis() - Clock.daysToMillis(numberOfEventsInDays.days)
        return targetEvents
            // 이벤트 키에 프로퍼티 없는 targetEvent 는 1개 이하가 보장
            .filter { it.eventKey == numberOfEventsInDays.eventKey }
            .firstOrNull { targetEvent ->
                return@firstOrNull targetEvent.property == null
            }
            .let { targetEvent ->
                if (targetEvent == null) {
                    return@let false
                }

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
        val daysAgoUtc = Clock.SYSTEM.currentMillis() - Clock.daysToMillis(numberOfEventsWithPropertyInDays.days)

        return targetEvents
            .filter { it.eventKey == numberOfEventsWithPropertyInDays.eventKey }
            .filter { targetEvent ->
                if (targetEvent.property == null) {
                    return@filter false
                }
                return@filter propertyMatch(targetEvent.property, numberOfEventsWithPropertyInDays.propertyFilter)
            }
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
    private fun propertyMatch(property: TargetEvent.Property, propertyCondition: Target.Condition): Boolean {
        if (propertyCondition.key.name == property.key) {
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
private val TargetEvent.countWithinDays: (Long) -> Int
    get() = { daysAgoUtc ->
        stats.filter { it.date >= daysAgoUtc }.sumOf { it.count }
    }
