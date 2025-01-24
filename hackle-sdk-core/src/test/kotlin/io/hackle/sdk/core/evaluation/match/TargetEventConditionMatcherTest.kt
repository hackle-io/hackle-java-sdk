package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.NUMBER_OF_EVENTS_IN_DAYS
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
            match(MATCH, Target.Match.Operator.IN, NUMBER, 42)
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
    fun `30일 내 발생한 타겟 이벤트가 1회도 없는 경우는 실패`() {
        verify(listOf(), "{\"eventKey\":\"purchase\",\"timeRange\":{\"period\":30,\"timeUnit\":\"DAYS\"}}", MATCH,
            Target.Match.Operator.IN, NUMBER, 1, false)
    }

    @Test
    fun `이벤트 key가 다르면 실패`() {
        verify(listOf(TargetEvent("purchase", makeTargetEventsStat(30, 1))), "{\"eventKey\":\"purchased\",\"timeRange\":{\"period\":30,\"timeUnit\":\"DAYS\"}}", MATCH,
            Target.Match.Operator.GT, NUMBER, 1, false)
    }

    @Test
    fun `매일 1회씩 purchase 이벤트가 발생했고, 30일 내 1회 이상 purchase 가 발생한 조건이 들어온 경우 성공`() {
        verify(listOf(TargetEvent("purchase", makeTargetEventsStat(30, 1))), "{\"eventKey\":\"purchase\",\"timeRange\":{\"period\":30,\"timeUnit\":\"DAYS\"}}", MATCH,
            Target.Match.Operator.GT, NUMBER, 1, true)
    }

    @Test
    fun `어제 3회 login 이벤트가 발생했고, 1일 내 3회 login 이벤트가 발생한 조건이 들어온 경우 성공`() {
        verify(listOf(TargetEvent("login", makeTargetEventsStat(1, 3)), TargetEvent("purchase", makeTargetEventsStat(1, 1))), "{\"eventKey\":\"login\",\"timeRange\":{\"period\":1,\"timeUnit\":\"DAYS\"}}", MATCH,
            Target.Match.Operator.IN, NUMBER, 3, true)
    }

    @Test
    fun `매일 3회씩 purchase 이벤트가 발생했고, 30일 내 100회 purchase 가 발생한 조건이 들어온 경우 실패`() {
        verify(listOf(TargetEvent("purchase", makeTargetEventsStat(30, 3))), "{\"eventKey\":\"purchase\",\"timeRange\":{\"period\":30,\"timeUnit\":\"DAYS\"}}", MATCH,
            Target.Match.Operator.GT, NUMBER, 100, false)
    }

    @Test
    fun `5일 전 purchase 이벤트가 milk properoty와 1회 발생했고, 최근 7일 내 purchase 이벤트가 milk property와 1회 이상 발생한 조건이 들어온 경우 성공`() {
        verify(listOf(TargetEvent("purchase", makeSingleTargetEventsStat(5, 1), TargetEvent.Property("productName", "milk"))), "{\"eventKey\":\"purchase\",\"timeRange\":{\"period\":7,\"timeUnit\":\"DAYS\"},\"filters\":[{\"propertyKey\":{\"type\":\"EVENT\",\"name\":\"productName\"},\"match\":{\"type\":\"MATCH\",\"operator\":\"IN\",\"valueType\":\"STRING\",\"values\":[\"milk\"]}}]}", MATCH,
            Target.Match.Operator.GTE, NUMBER, 1, true)
    }

    @Test
    fun `5일 전 purchase 이벤트가 milk properoty와 1회 발생했고, 최근 7일 내 purchase 이벤트가 milk property와 1회 초과 발생한 조건이 들어온 경우 실패`() {
        verify(listOf(TargetEvent("purchase", makeSingleTargetEventsStat(5, 1), TargetEvent.Property("productName", "milk"))), "{\"eventKey\":\"purchase\",\"timeRange\":{\"period\":7,\"timeUnit\":\"DAYS\"},\"filters\":[{\"propertyKey\":{\"type\":\"EVENT\",\"name\":\"productName\"},\"match\":{\"type\":\"MATCH\",\"operator\":\"IN\",\"valueType\":\"STRING\",\"values\":[\"milk\"]}}]}", MATCH,
            Target.Match.Operator.GT, NUMBER, 1, false)
    }

    @Test
    fun `매일 gold grade 등급의 로그인 이벤트가 2회 발생했고, 30일 내 gold grade 등급의 로그인 이벤트가 30회 이상 발생한 조건이 들어온 경우 성공`() {
        verify(listOf(TargetEvent("login", makeTargetEventsStat(30, 2), TargetEvent.Property("grade", "gold"))), "{\"eventKey\":\"login\",\"timeRange\":{\"period\":30,\"timeUnit\":\"DAYS\"},\"filters\":[{\"propertyKey\":{\"type\":\"EVENT\",\"name\":\"grade\"},\"match\":{\"type\":\"MATCH\",\"operator\":\"IN\",\"valueType\":\"STRING\",\"values\":[\"gold\"]}}]}", MATCH,
            Target.Match.Operator.GTE, NUMBER, 30, true)
    }

    @Test
    fun `매일 siver grade 등급의 로그인이 6회 발생했고, 30일 내 platinum grade 등급의 로그인 이벤트가 30회 이상 발생한 조건이 들어온 경우 실패`() {
        verify(listOf(TargetEvent("login", makeTargetEventsStat(30, 6), TargetEvent.Property("grade", "silver"))), "{\"eventKey\":\"login\",\"timeRange\":{\"period\":30,\"timeUnit\":\"DAYS\"},\"filters\":[{\"propertyKey\":{\"type\":\"EVENT\",\"name\":\"grade\"},\"match\":{\"type\":\"MATCH\",\"operator\":\"IN\",\"valueType\":\"STRING\",\"values\":[\"platinum\"]}}]}", MATCH,
            Target.Match.Operator.GTE, NUMBER, 30, false)
    }


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

    private fun makeTargetEventsStat(days: Int, numOfEventsInDay: Int = 1): List<TargetEvent.Stat> {
        val targetEvents = mutableListOf<TargetEvent.Stat>()
        for (i in 0 until days) {
            val timestamp = Clock.SYSTEM.currentMillis() - Clock.daysToMillis(i) + 2 * 60 * 60 * 1000L // 현재시간 - i일 + 2시간, TODO: 일단 테스트를 위해 임의로 적었는데 서버에서 어떻게 시간이 내려오는지 확인 필요
            targetEvents.add(TargetEvent.Stat(timestamp, numOfEventsInDay))
        }
        return targetEvents
    }

    private fun makeSingleTargetEventsStat(daysAgo: Int, numOfEventInDay: Int = 1): List<TargetEvent.Stat> {
        val targetEvents = mutableListOf<TargetEvent.Stat>()
        val timestamp = Clock.SYSTEM.currentMillis() - Clock.daysToMillis(daysAgo) - 2 * 60 * 60 * 1000L // 현재시간 - i일 - 2시간, TODO: 일단 테스트를 위해 임의로 적었는데 서버에서 어떻게 시간이 내려오는지 확인 필요
        targetEvents.add(TargetEvent.Stat(timestamp, numOfEventInDay))
        return targetEvents
    }
}