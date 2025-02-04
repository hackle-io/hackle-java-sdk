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
internal abstract class TargetSegmentationExpressionMatcher<T: Target.TargetSegmentationExpression> {
    protected abstract val valueOperatorMatcher: ValueOperatorMatcher
    protected abstract val clock: Clock
    internal val gson = Gson()


    /**
     * TargetEvent List 에서 Target.Condition 에 해당하는 이벤트가 있는지 확인
     */
    internal abstract fun match(targetEvents: List<TargetEvent>, condition: Target.Condition): Boolean

    /**
     * TargetEvent 에서 Target.TargetSegmentationExpression 에 해당하는 이벤트가 있는지 확인
     */
    internal abstract fun match(targetEvent: TargetEvent, targetSegmentationExpression: T): Boolean

    /**
     * Target.Key to Target.TargetSegmentationExpression
     */
    internal abstract fun Target.Key.toSegmentationExpression(): T
}

/**
 * NumberOfEventsInDaysMatcher
 *
 * 기간 내 이벤트 발생 횟수
 */
internal class NumberOfEventsInDaysMatcher(
    override val valueOperatorMatcher: ValueOperatorMatcher,
    override val clock: Clock
): TargetSegmentationExpressionMatcher<Target.TargetSegmentationExpression.NumberOfEventsInDays>() {

    override fun match(targetEvents: List<TargetEvent>, condition: Target.Condition): Boolean {
        val numberOfEventsInDays = condition.key.toSegmentationExpression()
        val daysAgoUtc = clock.currentMillis() - TimeUnit.DAYS.toMillis(numberOfEventsInDays.days.toLong())
        val eventCount = targetEvents
            .filter { match(it, numberOfEventsInDays) }
            .sumOf { it.countWithinDays(daysAgoUtc) }

        return valueOperatorMatcher.matches(eventCount, condition.match)
    }

    override fun match(targetEvent: TargetEvent, targetSegmentationExpression: Target.TargetSegmentationExpression.NumberOfEventsInDays): Boolean {
        return targetEvent.eventKey == targetSegmentationExpression.eventKey && targetEvent.property == null
    }

    override fun Target.Key.toSegmentationExpression(): Target.TargetSegmentationExpression.NumberOfEventsInDays {
        return gson.fromJson(name, Target.TargetSegmentationExpression.NumberOfEventsInDays::class.java)
    }
}

/**
 * NumberOfEventsInDaysWithPropertyMatcher
 *
 * 기간 내 특정 프로퍼티를 가진 이벤트 발생 횟수
 */
internal class NumberOfEventsWithPropertyInDaysMatcher(
    override val valueOperatorMatcher: ValueOperatorMatcher,
    override val clock: Clock
): TargetSegmentationExpressionMatcher<Target.TargetSegmentationExpression.NumberOfEventsWithPropertyInDays>()  {

    override fun match(targetEvents: List<TargetEvent>, condition: Target.Condition): Boolean {
        val numberOfEventsWithPropertyInDays = condition.key.toSegmentationExpression()
        val daysAgoUtc = clock.currentMillis() - TimeUnit.DAYS.toMillis(numberOfEventsWithPropertyInDays.days.toLong())
        val eventCount = targetEvents
            .filter { match(it, numberOfEventsWithPropertyInDays) }
            .sumOf { it.countWithinDays(daysAgoUtc) }

        return valueOperatorMatcher.matches(eventCount, condition.match)
    }

    override fun match(
        targetEvent: TargetEvent,
        targetSegmentationExpression: Target.TargetSegmentationExpression.NumberOfEventsWithPropertyInDays
    ): Boolean {
        return targetEvent.eventKey == targetSegmentationExpression.eventKey && propertyMatch(targetEvent.property, targetSegmentationExpression.propertyFilter)
    }

    override fun Target.Key.toSegmentationExpression(): Target.TargetSegmentationExpression.NumberOfEventsWithPropertyInDays {
        return gson.fromJson(name, Target.TargetSegmentationExpression.NumberOfEventsWithPropertyInDays::class.java)
    }

    /**
     * TargetEvent.Property 와 Target.Condition.Property 비교
     *
     * property의 key, type, value가 모두 일치하면 true
     * @param property TargetEvent.Property
     * @param propertyCondition Target.Condition.Property
     * @return Boolean
     */
    private fun propertyMatch(property: TargetEvent.Property?, propertyCondition: Target.Condition): Boolean {
        if (property?.type == propertyCondition.key.type && property.key == propertyCondition.key.name) {
            return valueOperatorMatcher.matches(property.value, propertyCondition.match)
        }
        return false
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
