package io.hackle.sdk.core.evaluation.match

import com.google.gson.Gson
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Match.Type.MATCH
import io.hackle.sdk.core.model.ValueType.*
import io.hackle.sdk.core.user.HackleUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.concurrent.TimeUnit

class TargetEventConditionMatcherTest {
    private val testClock = TestClock() // default KST 09:00:00

    private val numberOfEventsInDaysMatcher = NumberOfEventsInDaysMatcher(ValueOperatorMatcher(ValueOperatorMatcherFactory()), testClock)
    private val numberOfEventsWithPropertyInDaysMatcher = NumberOfEventsWithPropertyInDaysMatcher(ValueOperatorMatcher(ValueOperatorMatcherFactory()), testClock)
    private val sut = TargetEventConditionMatcher(numberOfEventsInDaysMatcher, numberOfEventsWithPropertyInDaysMatcher)

    @BeforeEach
    fun resetClock() {
        testClock.setKstTime(9)
    }

    @Test
    fun `올바르지 않는 type이 들어온 경우 실패`() {
        val request = experimentRequest(
            user = HackleUser.builder()
                .targetEvents(listOf())
                .build()
        )
        val condition = condition {
            key(Target.Key.Type.FEATURE_FLAG, "purchase")
            match(MATCH, Target.Match.Operator.GTE, NUMBER, 1)
        }

        val exception = assertThrows<IllegalArgumentException> {
            sut.matches(request, Evaluators.context(), condition)
        }

        expectThat(exception.message) isEqualTo "Unsupported Target.Key.Type [FEATURE_FLAG]"
    }

    @Test
    fun `올바르지 않는 property type이 들어온 경우 실패`() {
        val targetEvents = listOf(
            TargetEvent("purchase", makeSingleTargetEventsStat(5, 2), TargetEvent.Property("productName", Target.Key.Type.EVENT_PROPERTY, "milk")))
        verify(
            targetEvents = targetEvents,
            key = getKeyString(
                "purchase", 7,  Target.Condition(
                    Target.Key(Target.Key.Type.HACKLE_PROPERTY, "productName"),
                    Target.Match(MATCH, Target.Match.Operator.IN, STRING, listOf("milk"))
                )
            ),
            matchType = MATCH,
            operator =Target.Match.Operator.GTE,
            valueType = NUMBER,
            targetValue = 1,
            expected = false)
    }

    @Test
    fun `30일 내 발생한 타겟 이벤트가 1회도 없는 경우는 실패`() {
        verify(
            targetEvents = listOf(),
            key = getKeyString("purchase", 30),
            matchType = MATCH,
            operator = Target.Match.Operator.IN,
            valueType = NUMBER,
            targetValue = 1,
            expected = false
        )
    }

    @Test
    fun `이벤트 key가 다르면 실패`() {
        val targetEvents = listOf(TargetEvent("purchase", makeTargetEventsStat(30, 1)))
        verify(
            targetEvents = targetEvents,
            key = getKeyString("purchased", 30),
            matchType = MATCH,
            operator = Target.Match.Operator.GT,
            valueType = NUMBER,
            targetValue = 1,
            expected = false
        )
    }

    @Test
    fun `매일 1회씩 purchase 이벤트가 발생했고, 30일 내 1회 이상 purchase 가 발생한 조건이 들어온 경우 성공`() {
        val targetEvents = listOf(TargetEvent("purchase", makeTargetEventsStat(30, 1)))
        verify(
            targetEvents = targetEvents,
            key = getKeyString("purchase", 30),
            matchType = MATCH,
            operator = Target.Match.Operator.GT,
            valueType = NUMBER,
            targetValue = 1,
            expected = true)
    }

    @Test
    fun `오늘 3회 login 이벤트가 발생했고, 1일 내 3회 login 이벤트가 발생한 조건이 들어온 경우 성공`() {
        val targetEvents = listOf(
            TargetEvent("login", makeSingleTargetEventsStat(0, 3)),
            TargetEvent("purchase", makeTargetEventsStat(1, 1))
        )
        verify(
            targetEvents = targetEvents,
            key = getKeyString("login", 1),
            matchType = MATCH,
            operator = Target.Match.Operator.IN,
            valueType = NUMBER,
            targetValue = 3,
            expected = true)
    }

    @Test
    fun `오늘 09시 기준 어제 3회 login 이벤트가 발생했고, 1일 내 3회 login 이벤트가 발생한 조건이 들어온 경우 성공`() {
        val targetEvents = listOf(
            TargetEvent("login", makeSingleTargetEventsStat(1, 3)),
            TargetEvent("purchase", makeTargetEventsStat(1, 1))
        )
        verify(
            targetEvents = targetEvents,
            key = getKeyString("login", 1),
            matchType =MATCH,
            operator = Target.Match.Operator.IN,
            valueType = NUMBER,
            targetValue = 3,
            expected = true)
    }

    @Test
    fun `오늘 14시 기준으로 어제 3회 login 이벤트가 발생했고, 1일 내 3회 login 이벤트가 발생한 조건이 들어온 경우 실패`() {
        testClock.setKstTime(14)

        val targetEvents = listOf(
            TargetEvent("login", makeSingleTargetEventsStat(1, 3)),
            TargetEvent("purchase", makeTargetEventsStat(1, 1))
        )
        verify(
            targetEvents = targetEvents,
            key = getKeyString("login", 1),
            matchType =MATCH,
            operator = Target.Match.Operator.IN,
            valueType = NUMBER,
            targetValue = 3,
            expected = false)
    }

    @Test
    fun `매일 3회씩 purchase 이벤트가 발생했고, 30일 내 100회 purchase 가 발생한 조건이 들어온 경우 실패`() {
        val targetEvents = listOf(TargetEvent("purchase", makeTargetEventsStat(30, 3)))
        verify(
            targetEvents = targetEvents,
            key = getKeyString("purchase", 30),
            matchType = MATCH,
            operator =Target.Match.Operator.GT,
            valueType = NUMBER,
            targetValue = 100,
            expected = false)
    }

    @Test
    fun `5일 전 purchase 이벤트가 milk properoty와 1회 발생했고, 최근 7일 내 purchase 이벤트가 milk property와 1회 이상 발생한 조건이 들어온 경우 성공`() {
        val targetEvents = listOf(
            TargetEvent("purchase", makeSingleTargetEventsStat(5, 2), TargetEvent.Property("productName", Target.Key.Type.EVENT_PROPERTY, "milk")))
        verify(
            targetEvents = targetEvents,
            key = getKeyString(
                "purchase", 7,  Target.Condition(
                    Target.Key(Target.Key.Type.EVENT_PROPERTY, "productName"),
                    Target.Match(MATCH, Target.Match.Operator.IN, STRING, listOf("milk"))
                )
            ),
            matchType = MATCH,
            operator =Target.Match.Operator.GTE,
            valueType = NUMBER,
            targetValue = 1,
            expected = true)
    }

    @Test
    fun `5일 전 purchase 이벤트가 milk properoty와 1회 발생했고, 최근 7일 내 purchase 이벤트가 milk property와 1회 초과 발생한 조건이 들어온 경우 실패`() {
        val targetEvents = listOf(TargetEvent("purchase", makeSingleTargetEventsStat(4, 1), TargetEvent.Property("productName", Target.Key.Type.EVENT_PROPERTY, "milk")))
        verify(
            targetEvents = targetEvents,
            key = getKeyString(
                "purchase", 7, Target.Condition(
                    Target.Key(Target.Key.Type.EVENT_PROPERTY, "productName"),
                    Target.Match(MATCH, Target.Match.Operator.IN, STRING, listOf("milk"))
                )
            ),
            matchType = MATCH,
            operator = Target.Match.Operator.GT,
            valueType = NUMBER,
            targetValue = 1,
            expected = false)
    }

    @Test
    fun `1일 동안 purchase 이벤트가 milk property와 함께 매일 1회씩 발생했고 2일 동안 purchase 이벤트가 cookie property와 함께 2회씩 발생했는데, 최근 5일 내 purchase 이벤트가 5회 이상 발생하고 filter가  milk, cookie 인 경우 성공`() {
        val targetEvents = listOf(
            TargetEvent("purchase", makeTargetEventsStat(1, 1), TargetEvent.Property("productName", Target.Key.Type.EVENT_PROPERTY, "milk")),
            TargetEvent("purchase", makeTargetEventsStat(2, 2), TargetEvent.Property("productName", Target.Key.Type.EVENT_PROPERTY, "cookie")),
            TargetEvent("login", makeTargetEventsStat(3, 3)),
            TargetEvent("purchase", makeTargetEventsStat(3, 3))
        )
        verify(
            targetEvents= targetEvents,
            key = getKeyString(
                "purchase", 5, Target.Condition(
                    Target.Key(Target.Key.Type.EVENT_PROPERTY, "productName"),
                    Target.Match(MATCH, Target.Match.Operator.CONTAINS, STRING, listOf("cookie", "milk"))
                )
            ),
            matchType = MATCH,
            operator =Target.Match.Operator.GTE,
            valueType = NUMBER,
            targetValue = 5,
            expected = true)
    }

    @Test
    fun `7일 전 purchase 이벤트가 milk property와 함께 매일 1회씩 발생했고 2일 동안 purchase 이벤트가 cookie property와 함께 2회씩 발생했는데, 최근 7일 내 purchase 이벤트가 5회 발생하고 filter가  milk, cookie 인 경우 성공`() {
        val targetEvents = listOf(
            TargetEvent("purchase", makeSingleTargetEventsStat(7, 1), TargetEvent.Property("productName", Target.Key.Type.EVENT_PROPERTY, "milk")),
            TargetEvent("purchase", makeTargetEventsStat(2, 2), TargetEvent.Property("productName", Target.Key.Type.EVENT_PROPERTY, "cookie")),
        )
        verify(
            targetEvents= targetEvents,
            key = getKeyString(
                "purchase", 7, Target.Condition(
                    Target.Key(Target.Key.Type.EVENT_PROPERTY, "productName"),
                    Target.Match(MATCH, Target.Match.Operator.CONTAINS, STRING, listOf("cookie", "milk"))
                )
            ),
            matchType = MATCH,
            operator = Target.Match.Operator.IN,
            valueType = NUMBER,
            targetValue = 5,
            expected = true)
    }

    @Test
    fun `stat에 property가 있는 이벤트만 있는데 filter가 비어있는 경우 실패`() {
        val targetEvents = listOf(TargetEvent("purchase", makeTargetEventsStat(30, 1), TargetEvent.Property("productName", Target.Key.Type.EVENT_PROPERTY, "milk")))
        verify(
            targetEvents = targetEvents,
            key = getKeyString("purchase", 3),
            matchType = MATCH,
            operator =Target.Match.Operator.GTE,
            valueType = NUMBER,
            targetValue = 1,
            expected = false)
    }

    @Test
    fun `발생한 이벤트가 없는데 0회 발생 조건이면 성공`() {
        val targetEvents = listOf<TargetEvent>()
        verify(
            targetEvents = targetEvents,
            key = getKeyString("purchase", 1),
            matchType = MATCH,
            operator =Target.Match.Operator.GTE,
            valueType = NUMBER,
            targetValue = 0,
            expected = true)

        verify(
            targetEvents = targetEvents,
            key = getKeyString("purchase", 1, Target.Condition(
                Target.Key(Target.Key.Type.EVENT_PROPERTY, "productName"),
                Target.Match(MATCH, Target.Match.Operator.IN, STRING, listOf("milk"))
            )),
            matchType = MATCH,
            operator =Target.Match.Operator.GTE,
            valueType = NUMBER,
            targetValue = 0,
            expected = true)
    }

    @Test
    fun `발생 조건에 부합하지 않는 이벤트만 발생했는데, 부합 조건이 0이면 성공`() {
        val targetEvents = listOf(
            TargetEvent("purchase", makeSingleTargetEventsStat(1, 3), TargetEvent.Property("productName", Target.Key.Type.EVENT_PROPERTY, "smartphone")),
            TargetEvent("purchase", makeSingleTargetEventsStat(0, 1))
        )
        verify(
            targetEvents = targetEvents,
            key = getKeyString("login", 1),
            matchType = MATCH,
            operator =Target.Match.Operator.GTE,
            valueType = NUMBER,
            targetValue = 0,
            expected = true)

        verify(
            targetEvents = targetEvents,
            key = getKeyString(
                "purchase", 1, Target.Condition(
                    Target.Key(Target.Key.Type.EVENT_PROPERTY, "price"),
                    Target.Match(MATCH, Target.Match.Operator.GTE, NUMBER, listOf(10000))
                )
            ),
            matchType = MATCH,
            operator =Target.Match.Operator.GTE,
            valueType = NUMBER,
            targetValue = 0,
            expected = true)
    }

    @Test
    fun `발생 조건에 일부 부합하는 이벤트가 발생했고 부합조건도 만족하면 성공`() {
        val targetEvents = listOf(
            TargetEvent("purchase", makeSingleTargetEventsStat(1, 3), TargetEvent.Property("productName", Target.Key.Type.EVENT_PROPERTY, "cookie")),
            TargetEvent("purchase", makeSingleTargetEventsStat(0, 1))
        )

        verify(
            targetEvents = targetEvents,
            key = getKeyString(
                "purchase", 7, Target.Condition(
                    Target.Key(Target.Key.Type.EVENT_PROPERTY, "productName"),
                    Target.Match(MATCH, Target.Match.Operator.IN, STRING, listOf("milk", "cookie"))
                )
            ),
            matchType = MATCH,
            operator = Target.Match.Operator.GTE,
            valueType = NUMBER,
            targetValue = 1,
            expected = true)
    }

    @Test
    fun `발생 조건에 부합하는 이벤트만 발생했지만 부합조건이 0이면 실패`() {
        val targetEvents = listOf(
            TargetEvent("purchase", makeSingleTargetEventsStat(1, 3), TargetEvent.Property("productName", Target.Key.Type.EVENT_PROPERTY, "cookie")),
            TargetEvent("purchase", makeSingleTargetEventsStat(0, 1), TargetEvent.Property("productName", Target.Key.Type.EVENT_PROPERTY, "milk")),
            TargetEvent("purchase", makeSingleTargetEventsStat(0, 1))
        )

        verify(
            targetEvents = targetEvents,
            key = getKeyString(
                "purchase", 7, Target.Condition(
                    Target.Key(Target.Key.Type.EVENT_PROPERTY, "productName"),
                    Target.Match(MATCH, Target.Match.Operator.IN, STRING, listOf("milk", "cookie"))
                )
            ),
            matchType = MATCH,
            operator =Target.Match.Operator.IN,
            valueType = NUMBER,
            targetValue = 0,
            expected = false)
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
        val keyType = if(key.contains("propertyFilter")) {
            Target.Key.Type.NUMBER_OF_EVENTS_WITH_PROPERTY_IN_DAYS
        } else {
            Target.Key.Type.NUMBER_OF_EVENTS_IN_DAYS
        }

        val condition = condition {
            key(keyType, key)
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
     * @param daysAgo days 전
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
        val daysAgoMillis = TimeUnit.DAYS.toMillis(daysAgo.toLong())
        val timestamp = currentMillis - (currentMillis % (24 * 60 * 60 * 1000L)) - daysAgoMillis
        return timestamp
    }

    private fun getKeyString(eventKey: String, days: Int): String {
        val model = Target.TargetSegmentationExpression.TargetEvent.NumberOfEventsInDays(eventKey, days)
        return Gson().toJson(model)
    }

    private fun getKeyString(eventKey: String, days: Int, filter: Target.Condition): String {
        val model = Target.TargetSegmentationExpression.TargetEvent.NumberOfEventsWithPropertyInDays(eventKey, days, filter)
        return Gson().toJson(model)
    }

    /**
     * 오늘 기준으로 특정 KST의 UTC+0 timestamp를 반환하는 Clock
     */
    class TestClock(
        private var kstTime: Int = 9
    ) : Clock {
        override fun currentMillis(): Long {
            if (kstTime < 0 || kstTime > 23) {
                throw IllegalArgumentException("Invalid KST time: $kstTime")
            }

            val dayMillis = 24 * 60 * 60 * 1000L
            val kstOffset = 9 * 60 * 60 * 1000L // UTC+9 오프셋
            return (Clock.SYSTEM.currentMillis() + kstOffset) / dayMillis * dayMillis - kstOffset + kstTime * 60 * 60 * 1000L
        }

        override fun tick(): Long {
            return currentMillis() * 1_000_000L
        }

        fun setKstTime(kstTime: Int) {
            this.kstTime = kstTime
        }
    }

}