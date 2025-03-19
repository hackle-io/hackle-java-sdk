package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.model.Target
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.startsWith
import java.lang.IllegalArgumentException

@ExtendWith(MockKExtension::class)
internal class EventConditionMatcherTest {

    @MockK
    private lateinit var valueOperatorMatcher: ValueOperatorMatcher

    @MockK
    private lateinit var eventValueResolver: EventValueResolver

    lateinit var context: Evaluator.Context

    @InjectMockKs
    private lateinit var sut: EventConditionMatcher


    @BeforeEach
    fun beforeEach() {
        context = Evaluators.context()
    }

    @Test
    fun `eventRequest 가 아닌 경우는 false 를 리턴한다`() {
        val request = mockk<Evaluator.Request>()
        val condition = mockk<Target.Condition>()


        val result = sut.matches(request, context, condition)

        expectThat(result) isEqualTo false

    }

    @Test
    fun `eventValueResolver 는 이벤트 프로퍼티 타입만 처리한다`() {
        val request = mockk<Evaluator.EventRequest>()
        val condition = mockk<Target.Condition>()

        every { condition.key.type } returns Target.Key.Type.USER_ID
        every { request.event } returns mockk()
        every {
            eventValueResolver.resolveOrNull(any(), any())
        } throws IllegalArgumentException("Unsupported target key Type")

        val exception = assertThrows<IllegalArgumentException> {
            sut.matches(request, context, condition)
        }

        expectThat(exception.message)
            .isNotNull()
            .startsWith("Unsupported target key Type")

        verify { valueOperatorMatcher wasNot Called }
    }


    @Test
    fun `이벤트 프로퍼티 값이 맞으면 true를 리턴한다`() {
        val request = mockk<Evaluator.EventRequest>()
        val condition = mockk<Target.Condition>()

        every { condition.key.type } returns Target.Key.Type.EVENT_PROPERTY
        every { condition.match } returns mockk()
        every { request.event } returns mockk()
        every {
            eventValueResolver.resolveOrNull(any(), any())
        } returns mockk()
        every { valueOperatorMatcher.matches(any(), any()) } returns true

        val result = sut.matches(request, context, condition)

        expectThat(result) isEqualTo true
    }
}
