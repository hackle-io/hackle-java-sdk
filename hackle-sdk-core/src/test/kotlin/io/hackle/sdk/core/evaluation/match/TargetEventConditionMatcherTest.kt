package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.NUMBER_OF_EVENTS_IN_DAYS
import io.hackle.sdk.core.model.Target.Match.Operator.IN
import io.hackle.sdk.core.model.Target.Match.Type.MATCH
import io.hackle.sdk.core.model.ValueType.NUMBER
import io.hackle.sdk.core.user.HackleUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class TargetEventConditionMatcherTest {

    private val numberOfEventsInDaysMatcher = NumberOfEventsInDaysMatcher(ValueOperatorMatcher(ValueOperatorMatcherFactory()))
    private val sut = TargetEventConditionMatcher(numberOfEventsInDaysMatcher)

    @Test
    fun `when condition key type is not NUMBER_OF_EVENTS_IN_DAYS then throw exception`() {
        // given
        val request = experimentRequest()
        val condition = condition {
            key(Target.Key.Type.USER_PROPERTY, "age")
            match(MATCH, IN, NUMBER, 42)
        }

        // when
        val actual = assertThrows<IllegalArgumentException> {
            sut.matches(request, Evaluators.context(), condition)
        }

        // then
        expectThat(actual.message)
            .isNotNull()
            .isEqualTo("Unsupported Target.Key.Type [USER_PROPERTY]")
    }

    @Test
    fun `matches`() {
        // 이벤트가 조건에 맞는지 확인하는 테스트 케이스들
        verify(listOf(), "{\"eventKey\":\"purchase\",\"timeRange\":{\"period\":30,\"timeUnit\":\"DAYS\"}}", MATCH, IN, NUMBER, 1, false)
        val timestamp = Clock.SYSTEM.currentMillis() - Clock.daysToMillis(1) + 2 * 60 * 60 * 1000L
        verify(listOf(TargetEvent("purchase", listOf(TargetEvent.Stat(timestamp, 3)))), "{\"eventKey\":\"purchase\",\"timeRange\":{\"period\":1,\"timeUnit\":\"DAYS\"}}", MATCH, IN, NUMBER, 3, true)

       // verify(MATCH, listOf(TargetEvent("event1")), listOf("event1"), true)
       // verify(MATCH, listOf(TargetEvent("event1")), listOf("event2"), false)
       // verify(MATCH, listOf(TargetEvent("event1"), TargetEvent("event2")), listOf("event1", "event2"), true)
       // verify(MATCH, listOf(TargetEvent("event1"), TargetEvent("event2")), listOf("event3"), false)
    }//

    private fun verify(targetEvents: List<TargetEvent>, key: String, matchType: Target.Match.Type, operator: Target.Match.Operator, valueType: ValueType, targetValue: Any, expected: Boolean) {
        val request = experimentRequest(
            user = HackleUser.builder()
                .targetEvents(targetEvents)
                .build()
        )

        val condition = condition {
            key(NUMBER_OF_EVENTS_IN_DAYS, key)
            match(matchType, operator, valueType, targetValue)
        }

        val actual = sut.matches(request, Evaluators.context(), condition)

        assertEquals(expected, actual)
    }
}