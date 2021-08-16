package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.common.User
import io.hackle.sdk.core.model.Segment
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
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class SegmentMatcherTest {

    @MockK
    private lateinit var propertyConditionMatcher: PropertyConditionMatcher

    @InjectMockKs
    private lateinit var sut: SegmentMatcher

    @Test
    fun `Segment 대상의 조건중 하나라도 일치하면 match true`() {
        // given
        val segment = Segment(
            32,
            Target(
                listOf(
                    condition(Target.Key.Type.USER_PROPERTY, false),
                    condition(Target.Key.Type.USER_PROPERTY, false),
                    condition(Target.Key.Type.USER_PROPERTY, false),
                    condition(Target.Key.Type.USER_PROPERTY, true),
                    condition(Target.Key.Type.USER_PROPERTY, false),
                )
            )
        )

        // when
        val actual = sut.matches(segment, mockk(), User.of("test"))

        // then
        assertTrue(actual)
        verify(exactly = 4) {
            propertyConditionMatcher.matches(any(), any(), any())
        }
    }

    @Test
    fun `Segment 대상의 조건들중 ConditionKeyType이 SEGMENT인 것은 제외하고 평가한다`() {
        // given
        val segment = Segment(
            32,
            Target(
                listOf(
                    condition(Target.Key.Type.SEGMENT, false),
                    condition(Target.Key.Type.SEGMENT, false),
                    condition(Target.Key.Type.SEGMENT, false),
                    condition(Target.Key.Type.USER_PROPERTY, false),
                    condition(Target.Key.Type.USER_PROPERTY, false),
                )
            )
        )

        // when
        val actual = sut.matches(segment, mockk(), User.of("test"))

        // then
        assertFalse(actual)
        verify(exactly = 2) {
            propertyConditionMatcher.matches(any(), any(), any())
        }
    }

    private fun condition(type: Target.Key.Type, isMatch: Boolean): Target.Condition {
        val condition = mockk<Target.Condition> {
            every { key } returns Target.Key(type, "name")
        }
        every { propertyConditionMatcher.matches(condition, any(), any()) } returns isMatch
        return condition
    }
}