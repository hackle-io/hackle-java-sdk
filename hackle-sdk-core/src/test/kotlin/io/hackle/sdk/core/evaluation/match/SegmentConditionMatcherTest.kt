package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.common.User
import io.hackle.sdk.core.model.Segment
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Match.Operator.IN
import io.hackle.sdk.core.model.Target.Match.Type.MATCH
import io.hackle.sdk.core.model.Target.Match.ValueType.NUMBER
import io.hackle.sdk.core.workspace.Workspace
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@ExtendWith(MockKExtension::class)
internal class SegmentConditionMatcherTest {

    @MockK
    private lateinit var workspace: Workspace

    @MockK
    private lateinit var segmentMatcher: SegmentMatcher

    @InjectMockKs
    private lateinit var sut: SegmentConditionMatcher

    @BeforeEach
    fun beforeEach() {
        every { workspace.getSegmentOrNull(any()) } returns null
    }

    @Test
    fun `ConditionKeyType이 SEGMENT가 아닌 경우 예외 발생`() {
        // given
        val condition = mockk<Target.Condition> {
            every { key } returns Target.Key(Target.Key.Type.USER_PROPERTY, "age")
        }

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.matches(condition, mockk(), User.of("abc"))
        }

        // then
        expectThat(exception.message)
            .isNotNull()
            .isEqualTo("Condition key type must be SEGMENT")
    }

    @Test
    fun `조건값들에 해당하는 Segement중 하나라도 일치하면 match true`() {
        // given
        mockSegment(42, false)
        mockSegment(52, false)
        mockSegment(320, true)


        val condition = Target.Condition(
            key = Target.Key(Target.Key.Type.SEGMENT, "segment"),
            match = Target.Match(
                type = MATCH,
                operator = IN,
                valueType = NUMBER,
                values = listOf("seg", 1, 42, 52, 320, 999)
            )
        )

        // when
        val actual = sut.matches(condition, workspace, User.of("test_id"))

        // then
        assertTrue(actual)
        verify(exactly = 3) {
            segmentMatcher.matches(any(), any(), any())
        }
    }

    private fun mockSegment(id: Long, isMatch: Boolean) {
        val segment = Segment(id, mockk())
        every { workspace.getSegmentOrNull(id) } returns segment
        every { segmentMatcher.matches(segment, any(), any()) } returns isMatch
    }
}