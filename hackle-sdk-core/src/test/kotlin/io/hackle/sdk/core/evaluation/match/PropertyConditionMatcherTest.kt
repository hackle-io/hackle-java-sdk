package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.common.User
import io.hackle.sdk.core.model.Target
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isNotNull
import strikt.assertions.startsWith

@ExtendWith(MockKExtension::class)
internal class PropertyConditionMatcherTest {

    @MockK
    private lateinit var valueOperatorMatcher: ValueOperatorMatcher

    @InjectMockKs
    private lateinit var sut: PropertyConditionMatcher


    @Test
    fun `TargetKeyType이 Segment인 경우 예외발생`() {
        // given
        val condition = mockk<Target.Condition> {
            every { key } returns Target.Key(Target.Key.Type.SEGMENT, "segment")
        }

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.matches(condition, mockk(), User.of("1"))
        }

        // then
        expectThat(exception.message)
            .isNotNull()
            .startsWith("Unsupported type")
    }

    @Test
    fun `TargetKeyType이 HACKLE_PROPERTY인 경우 match false`() {
        // given
        val condition = mockk<Target.Condition> {
            every { key } returns Target.Key(Target.Key.Type.HACKLE_PROPERTY, "platform")
        }

        // when
        val actual = sut.matches(condition, mockk(), User.of("1"))

        // then
        assertFalse(actual)
    }

    @Test
    fun `USER_PROPRTY에 해당하는 값이 없는 경우 match false`() {
        // given
        val condition = mockk<Target.Condition> {
            every { key } returns Target.Key(Target.Key.Type.USER_PROPERTY, "age")
        }


        // when
        val actual = sut.matches(condition, mockk(), User.of("1"))

        // then
        assertFalse(actual)
    }

    @Test
    fun `USER_PROPRTY에 해당하는 속성값을 가져와서 valueOperator로 매칭한다`() {
        // given
        val match = mockk<Target.Match>()
        val condition = mockk<Target.Condition> {
            every { key } returns Target.Key(Target.Key.Type.USER_PROPERTY, "age")
            every { this@mockk.match } returns match
        }

        val user = User.builder("13").property("age", 30).build()


        every { valueOperatorMatcher.matches(any(), any()) } returns true

        // when
        val actual = sut.matches(condition, mockk(), user)

        // then
        assertTrue(actual)
        verify {
            valueOperatorMatcher.matches(30, match)
        }
    }
}
