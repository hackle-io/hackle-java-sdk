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
    fun `condition 타입이 NUMBER_OF_EVENTS_IN_DAYS가 아니면 실패`() {
        // given
        val request = experimentRequest()
        val condition = condition {
            key(Target.Key.Type.USER_PROPERTY, "age")
            match(MATCH, Target.Match.Operator.IN, NUMBER, 42)
        }
        // when
        val actualSut = assertThrows<IllegalArgumentException> {
            sut.matches(request, Evaluators.context(), condition)
        }

        expectThat(actualSut.message)
            .isNotNull()
            .isEqualTo("Unsupported Target.Key.Type [USER_PROPERTY]")

        // when
        val actualMatcher = assertThrows<IllegalArgumentException> {
            numberOfEventsInDaysMatcher.matches(request.user.targetEvents, condition)
        }

        expectThat(actualMatcher.message)
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
    fun `오늘 3회 login 이벤트가 발생했고, 1일 내 3회 login 이벤트가 발생한 조건이 들어온 경우 성공`() {
        verify(listOf(TargetEvent("login", makeSingleTargetEventsStat(0, 3)), TargetEvent("purchase", makeTargetEventsStat(1, 1))), "{\"eventKey\":\"login\",\"timeRange\":{\"period\":1,\"timeUnit\":\"DAYS\"}}", MATCH,
            Target.Match.Operator.IN, NUMBER, 3, true)
    }

    @Test
    fun `어제 3회 login 이벤트가 발생했고, 1일 내 3회 login 이벤트가 발생한 조건이 들어온 경우 실패`() {
        verify(listOf(TargetEvent("login", makeSingleTargetEventsStat(1, 3)), TargetEvent("purchase", makeTargetEventsStat(1, 1))), "{\"eventKey\":\"login\",\"timeRange\":{\"period\":1,\"timeUnit\":\"DAYS\"}}", MATCH,
            Target.Match.Operator.IN, NUMBER, 3, false)
    }

    @Test
    fun `매일 3회씩 purchase 이벤트가 발생했고, 30일 내 100회 purchase 가 발생한 조건이 들어온 경우 실패`() {
        verify(listOf(TargetEvent("purchase", makeTargetEventsStat(30, 3))), "{\"eventKey\":\"purchase\",\"timeRange\":{\"period\":30,\"timeUnit\":\"DAYS\"}}", MATCH,
            Target.Match.Operator.GT, NUMBER, 100, false)
    }

    @Test
    fun `5일 전 purchase 이벤트가 발생했고, 최근 7일 내 purchase 이벤트가 milk property와 1회 이상 발생한 조건이 들어온 경우 실패`() {
        verify(listOf(TargetEvent("purchase", makeSingleTargetEventsStat(5, 2))), "{\"eventKey\":\"purchase\",\"timeRange\":{\"period\":7,\"timeUnit\":\"DAYS\"},\"filters\":[{\"propertyKey\":{\"type\":\"EVENT\",\"name\":\"productName\"},\"match\":{\"type\":\"MATCH\",\"operator\":\"IN\",\"valueType\":\"STRING\",\"values\":[\"milk\"]}}]}", MATCH,
            Target.Match.Operator.GTE, NUMBER, 1, false)
    }

    @Test
    fun `5일 전 purchase 이벤트가 milk properoty와 1회 발생했고, 최근 7일 내 purchase 이벤트가 milk property와 1회 이상 발생한 조건이 들어온 경우 성공`() {
        verify(listOf(TargetEvent("purchase", makeSingleTargetEventsStat(5, 2), TargetEvent.Property("productName", "milk"))), "{\"eventKey\":\"purchase\",\"timeRange\":{\"period\":7,\"timeUnit\":\"DAYS\"},\"filters\":[{\"propertyKey\":{\"type\":\"EVENT\",\"name\":\"productName\"},\"match\":{\"type\":\"MATCH\",\"operator\":\"IN\",\"valueType\":\"STRING\",\"values\":[\"milk\"]}}]}", MATCH,
            Target.Match.Operator.GTE, NUMBER, 1, true)
    }

    @Test
    fun `5일 전 purchase 이벤트가 milk properoty와 1회 발생했고, 최근 7일 내 purchase 이벤트가 milk property와 1회 초과 발생한 조건이 들어온 경우 실패`() {
        verify(listOf(TargetEvent("purchase", makeSingleTargetEventsStat(4, 1), TargetEvent.Property("productName", "milk"))), "{\"eventKey\":\"purchase\",\"timeRange\":{\"period\":7,\"timeUnit\":\"DAYS\"},\"filters\":[{\"propertyKey\":{\"type\":\"EVENT\",\"name\":\"productName\"},\"match\":{\"type\":\"MATCH\",\"operator\":\"IN\",\"valueType\":\"STRING\",\"values\":[\"milk\"]}}]}", MATCH,
            Target.Match.Operator.GT, NUMBER, 1, false)
    }

    @Test
    fun `6일 전 purchase 이벤트가 cookie property 그리고 금액 13000원 이벤트와 함께 1회 발생했고, 최근 7일 내 cookie를 10000원 이상 구매한 이벤트가 1회 이상 발생한 조건이 들어온 경우 성공`() {
        verify(listOf(
            TargetEvent("purchase", makeSingleTargetEventsStat(6, 1), TargetEvent.Property("productName", "cookie")),
            TargetEvent("purchase", makeSingleTargetEventsStat(6, 1), TargetEvent.Property("price", 13000))
        ), "{\"eventKey\":\"purchase\",\"timeRange\":{\"period\":7,\"timeUnit\":\"DAYS\"},\"filters\":[{\"propertyKey\":{\"type\":\"EVENT\",\"name\":\"productName\"},\"match\":{\"type\":\"MATCH\",\"operator\":\"IN\",\"valueType\":\"STRING\",\"values\":[\"cookie\"]}}, {\"propertyKey\":{\"type\":\"EVENT\",\"name\":\"price\"},\"match\":{\"type\":\"MATCH\",\"operator\":\"GTE\",\"valueType\":\"NUMBER\",\"values\":[10000]}}]}", MATCH,
            Target.Match.Operator.GTE, NUMBER, 1, true)
    }

    @Test
    fun `6일 전 purchase 이벤트가 cookie property 그리고 금액 13000원 이벤트와 함께 1회 발생했고, 최근 7일 내 cookie를 15000원 이상 구매한 이벤트가 1회 이상 발생한 조건이 들어온 경우 실패`() {
        verify(listOf(
            TargetEvent("purchase", makeSingleTargetEventsStat(6, 1), TargetEvent.Property("productName", "cookie")),
            TargetEvent("purchase", makeSingleTargetEventsStat(6, 1), TargetEvent.Property("price", 13000))
        ), "{\"eventKey\":\"purchase\",\"timeRange\":{\"period\":7,\"timeUnit\":\"DAYS\"},\"filters\":[{\"propertyKey\":{\"type\":\"EVENT\",\"name\":\"productName\"},\"match\":{\"type\":\"MATCH\",\"operator\":\"IN\",\"valueType\":\"STRING\",\"values\":[\"cookie\"]}}, {\"propertyKey\":{\"type\":\"EVENT\",\"name\":\"price\"},\"match\":{\"type\":\"MATCH\",\"operator\":\"GTE\",\"valueType\":\"NUMBER\",\"values\":[15000]}}]}", MATCH,
            Target.Match.Operator.GTE, NUMBER, 1, false)
    }

    // TODO: NOTE: 이 조건 우선 서버에서 동일한 propertyKey가 안들어 오게 막는다고 했는데, 이 경우는 어떻게 처리해야할지 논의 필요
    @Test
    fun `7일 동안 purchase 이벤트가 milk property와 함께 매일 1회씩 발생했고 3일 동안 purchase 이벤트가 cookie property와 함께 2회씩 발생했는데, 최근 5일 내 purchase 이벤트가 5회 발생했는데 filter가  milk, cookie 인 경우 성공`() {
        verify(
            listOf(
                TargetEvent("purchase", makeTargetEventsStat(5, 1), TargetEvent.Property("productName", "milk")),
                TargetEvent("purchase", makeTargetEventsStat(3, 2), TargetEvent.Property("productName", "cookie"))
            ), "{\"eventKey\":\"purchase\",\"timeRange\":{\"period\":5,\"timeUnit\":\"DAYS\"},\"filters\":[{\"propertyKey\":{\"type\":\"EVENT\",\"name\":\"productName\"},\"match\":{\"type\":\"MATCH\",\"operator\":\"IN\",\"valueType\":\"STRING\",\"values\":[\"milk\"]}}, {\"propertyKey\":{\"type\":\"EVENT\",\"name\":\"productName\"},\"match\":{\"type\":\"MATCH\",\"operator\":\"IN\",\"valueType\":\"STRING\",\"values\":[\"cookie\"]}}]}", MATCH,
            Target.Match.Operator.GTE, NUMBER, 5, true)
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

    @Test
    fun `stat에 property가 있는 이벤트만 있는데 filter가 비어있는 경우 실패`() {
        verify(listOf(TargetEvent("purchase", makeTargetEventsStat(30, 1), TargetEvent.Property("productName", "milk"))), "{\"eventKey\":\"purchase\",\"timeRange\":{\"period\":30,\"timeUnit\":\"DAYS\", \"filters\": null}}", MATCH,
            Target.Match.Operator.GTE, NUMBER, 1, false)

        verify(listOf(TargetEvent("purchase", makeTargetEventsStat(30, 1), TargetEvent.Property("productName", "milk"))), "{\"eventKey\":\"purchase\",\"timeRange\":{\"period\":30,\"timeUnit\":\"DAYS\",\"filters\":[]}}", MATCH,
            Target.Match.Operator.GTE, NUMBER, 1, false)
    }

    /**
     * TargetEvent.Condition 매칭 테스트
     *
     * expected와 다른 결과가 나오는 경우 실패
     * @param targetEvents TargetEvent 리스트
     * @param key Target.Condition key
     * @param matchType Target.Condition match type
     * @param operator Target.Condition operator
     * @param valueType Target.Condition value type
     * @param targetValue Target.Condition value
     * @param expected 예상 결과
     */
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

    /**
     * 오늘 기준으로 days 전까지의 TargetEvent.Stat 리스트를 생성한다.
     * @param days days 전
     * @param numOfEventsInDay 하루에 발생한 이벤트 수
     * @return TargetEvent.Stat 리스트
     */
    private fun makeTargetEventsStat(days: Int, numOfEventsInDay: Int = 1): List<TargetEvent.Stat> {
        val targetEvents = mutableListOf<TargetEvent.Stat>()
        for (i in 0 until days) {
            val timestamp = getTimeStamp(i)
            targetEvents.add(TargetEvent.Stat(timestamp, numOfEventsInDay))
        }
        return targetEvents
    }

    /**
     * 오늘 기준으로 days 전 해당 일에 TargetEvent.Stat 를 생성한다.
     *
     * 타겟이벤트에 Stat이 리스트로만 들어가 있어야 하기 때문에 리스트를 반환한다.
     * @param days days 전
     * @param numOfEventInDay 해당 일에 발생한 이벤트 수
     * @return TargetEvent.Stat 리스트
     */
    private fun makeSingleTargetEventsStat(daysAgo: Int, numOfEventInDay: Int = 1): List<TargetEvent.Stat> {
        val targetEvents = mutableListOf<TargetEvent.Stat>()
        val timestamp = getTimeStamp(daysAgo)
        targetEvents.add(TargetEvent.Stat(timestamp, numOfEventInDay))
        return targetEvents
    }

    /**
     * 오늘 기준으로 daysAgo 일 전의 timestamp를 반환한다.
     *
     * daysAgo 일의 00:00:00의 timestamp를 반환한다.
     *
     * @param daysAgo daysAgo 일 전
     * @return timestamp
     */
    private fun getTimeStamp(daysAgo: Int): Long {
        val currentMillis = Clock.SYSTEM.currentMillis()
        val daysAgoMillis = Clock.daysToMillis(daysAgo)
        val timestamp = currentMillis - (currentMillis % (24 * 60 * 60 * 1000L)) - daysAgoMillis
        return timestamp
    }
}