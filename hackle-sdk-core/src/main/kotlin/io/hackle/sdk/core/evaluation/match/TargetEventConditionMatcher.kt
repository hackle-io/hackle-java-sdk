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

internal abstract class TargetSegmentationExpressionMatcher {
    protected abstract val valueOperatorMatcher: ValueOperatorMatcher
    internal val gson = Gson()

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
        val filteredTargetEvents = targetEvents
            .filter { it.eventKey == numberOfEventsInDays.eventKey }

        return if (numberOfEventsInDays.filters.isNullOrEmpty()) {
            matchWithoutProperty(filteredTargetEvents, daysAgoUtc, condition)
        } else {
            matchWithProperty(filteredTargetEvents, numberOfEventsInDays.filters, daysAgoUtc, condition)
        }
    }

    private fun matchWithoutProperty(
        filteredTargetEvents: List<TargetEvent>,
        daysAgoUtc: Long,
        condition: Target.Condition
    ): Boolean {
        val numOfEvents = filteredTargetEvents
            .firstOrNull { it.property == null }
            ?.stats
            ?.filter { it.date >= daysAgoUtc }
            ?.sumOf { it.count }
            ?: return false

        return valueOperatorMatcher.matches(numOfEvents, condition.match)
    }

    private fun matchWithProperty(
        filteredTargetEvents: List<TargetEvent>,
        filters: List<TargetSegmentationOption.PropertyFilter>,
        daysAgoUtc: Long,
        condition: Target.Condition
    ): Boolean {
        return filters.all { propertyFilter ->
            val numOfEvents = filteredTargetEvents
                .firstOrNull { it.property?.key == propertyFilter.propertyKey.name }
                ?.takeIf { event ->
                    event.property?.let { valueOperatorMatcher.matches(it.value, propertyFilter.match) } == true
                }
                ?.stats
                ?.filter { it.date >= daysAgoUtc }
                ?.sumOf { it.count }
                ?: return false

            valueOperatorMatcher.matches(numOfEvents, condition.match)
        }
    }

    private fun Target.Key.toNumberOfEventsInDays(): TargetSegmentationExpression.NumberOfEventsInDays {
        require(type == NUMBER_OF_EVENTS_IN_DAYS) { "Unsupported Target.Key.Type [${type}]" }
        return gson.fromJson(name, TargetSegmentationExpression.NumberOfEventsInDays::class.java)
    }
}
