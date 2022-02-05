package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Segment
import io.hackle.sdk.core.model.Target
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class SegmentMatcherTest {

    @MockK
    private lateinit var userConditionMatcher: UserConditionMatcher

    @InjectMockKs
    private lateinit var sut: SegmentMatcher

    @Test
    fun `Target 이 비어있으면 match false`() {
        // given
        val segment = Segment(1, "seg1", type = Segment.Type.USER_PROPERTY, targets = emptyList())

        // when
        val actual = sut.matches(segment, mockk(), mockk())

        // then
        assertFalse(actual)
    }

    @Test
    fun `하나라도 매칭되는 Target 이 있으면 true`() {
        // given
        val segment = segment(
            listOf(true, true, true, false), // false
            listOf(false), // false
            listOf(true, true) // true
        )

        // when
        val actual = sut.matches(segment, mockk(), mockk())

        // then
        assertTrue(actual)
    }

    @Test
    fun `하나라도 매칭되는 Target 이 없으면 false`() {
        // given
        val segment = segment(
            listOf(true, true, true, false), // false
            listOf(false), // false
            listOf(false, true) // false
        )

        // when
        val actual = sut.matches(segment, mockk(), mockk())

        // then
        assertFalse(actual)
    }

    private fun segment(vararg targetConditions: List<Boolean>): Segment {
        val targets = mutableListOf<Target>()
        for (targetMatches in targetConditions) {
            val conditions = mutableListOf<Target.Condition>()
            for (conditionMatch in targetMatches) {
                val condition = mockk<Target.Condition>()
                every { userConditionMatcher.matches(condition, any(), any()) } returns conditionMatch
                conditions += condition
            }
            targets += Target(conditions)
        }
        return Segment(42, "seg", Segment.Type.USER_PROPERTY, targets)
    }
}