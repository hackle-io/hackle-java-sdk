package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Segment
import io.hackle.sdk.core.model.Target.Key.Type.SEGMENT
import io.hackle.sdk.core.model.Target.Key.Type.USER_PROPERTY
import io.hackle.sdk.core.model.Target.Match.Operator.IN
import io.hackle.sdk.core.model.condition
import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.core.workspace.workspace
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
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@ExtendWith(MockKExtension::class)
internal class SegmentConditionMatcherTest {

    @MockK
    private lateinit var segmentMatcher: SegmentMatcher

    @InjectMockKs
    private lateinit var sut: SegmentConditionMatcher

    @Test
    fun `KeyType이 SEGMENT 가 아니면 예외가 발생한다`() {
        // given
        val condition = condition {
            USER_PROPERTY("age")
            IN(42)
        }

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.matches(condition, mockk(), mockk())
        }

        // then
        expectThat(exception.message)
            .isNotNull()
            .isEqualTo("Unsupported target.key.type [USER_PROPERTY]")
    }

    @Test
    fun `등록된 segmentKey 가 String 타입이 아니면 필터링 한다`() {
        // given
        val condition = condition {
            SEGMENT("SEGMENT")
            IN(1, 2, 3)
        }

        // when
        val actual = sut.matches(condition, mockk(), mockk())

        // then
        assertFalse(actual)
    }

    @Test
    fun `등록된 segmentKey 에 해당하는 Segment 가 없으면 필터링 한다`() {
        // given
        val condition = condition {
            SEGMENT("SEGMENT")
            IN("seg1", "seg2")
        }

        val workspace = workspace()

        // when
        val actual = sut.matches(condition, workspace, mockk())

        // then
        assertFalse(actual)
    }

    @Test
    fun `등록된 segment 중 일치하는게 하나라도 있으면 true`() {
        // given
        val condition = condition {
            SEGMENT("SEGMENT")
            IN("seg1", "seg2", "seg3")
        }

        val workspace = mockk<Workspace>()
        segment(workspace, "seg1", false)
        segment(workspace, "seg2", true)
        segment(workspace, "seg3", false)

        // when
        val actual = sut.matches(condition, workspace, mockk())

        // then
        assertTrue(actual)
        verify(exactly = 2) {
            segmentMatcher.matches(any(), any(), any())
        }
    }

    private fun segment(workspace: Workspace, key: String, isMatch: Boolean): Segment {
        val segment = mockk<Segment>()
        every { segmentMatcher.matches(segment, any(), any()) } returns isMatch
        every { workspace.getSegmentOrNull(key) } returns segment
        return segment
    }
}