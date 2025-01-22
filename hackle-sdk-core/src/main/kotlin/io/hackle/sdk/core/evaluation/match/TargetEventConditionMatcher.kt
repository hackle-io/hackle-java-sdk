package io.hackle.sdk.core.evaluation.match

import com.google.gson.Gson
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.NUMBER_OF_EVENTS_IN_DAYS
import io.hackle.sdk.core.model.TargetSegmentationExpression
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * TargetEventConditionMatcher
 *
 * 실시간 타겟팅
 * TODO: 명칭 결정되면 객체명 수정 필요
 */
internal class TargetEventConditionMatcher(
    private val valueOperatorMatcher: ValueOperatorMatcher
): ConditionMatcher {
    override fun matches(request: Evaluator.Request, context: Evaluator.Context, condition: Target.Condition): Boolean {
        val numberOfEventsInDays = condition.key.toNumberOfEventsInDays()
        val periodDays = numberOfEventsInDays.timeRange.periodDays
        val daysAgoUtc = ZonedDateTime.now(ZoneOffset.UTC)
            .minusDays(periodDays.toLong())
            .toInstant()
            .toEpochMilli()

        val numOfEvents = request.user.targetEvents
            .first { it.eventKey == numberOfEventsInDays.eventKey && it.property == null }
            .stats
            .filter { it.date >= daysAgoUtc }
            .sumOf { it.count }

        return valueOperatorMatcher.matches(numOfEvents, condition.match)
    }
}

fun Target.Key.toNumberOfEventsInDays(): TargetSegmentationExpression.NumberOfEventsInDays {
    require(type == NUMBER_OF_EVENTS_IN_DAYS) { "Unsupported Target.Key.Type [${type}]" }
    return Gson().fromJson(name, TargetSegmentationExpression.NumberOfEventsInDays::class.java)
}