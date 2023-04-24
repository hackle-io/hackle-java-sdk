package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.model.Segment
import io.hackle.sdk.core.model.Target.Key.Type.SEGMENT
import io.hackle.sdk.core.model.Target.Key.Type.USER_PROPERTY
import io.hackle.sdk.core.model.Target.Match.Operator.IN
import io.hackle.sdk.core.model.Target.Match.Type.NOT_MATCH
import io.hackle.sdk.core.model.ValueType.STRING
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
            sut.matches(mockk(), mockk(), condition)
        }

        // then
        expectThat(exception.message)
            .isNotNull()
            .isEqualTo("Unsupported target.key.type [USER_PROPERTY]")
    }

    @Test
    fun `등록된 segmentKey 가 String 타입이 아니면 예외가 발생한다`() {
        // given
        val condition = condition {
            SEGMENT("SEGMENT")
            IN(1, 2, 3)
        }

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.matches(mockk(), mockk(), condition)
        }

        // then
        expectThat(exception.message)
            .isNotNull()
            .isEqualTo("SegmentKey[1]")
    }

    @Test
    fun `등록된 segmentKey 에 해당하는 Segment 가 없으면 예외가 발생한다`() {
        // given
        val condition = condition {
            SEGMENT("SEGMENT")
            IN("seg1", "seg2")
        }

        val workspace = workspace()
        val request = experimentRequest(workspace)

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.matches(request, Evaluators.context(), condition)
        }

        // then
        expectThat(exception.message)
            .isNotNull()
            .isEqualTo("Segment[seg1]")
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

        val request = experimentRequest(workspace)

        // when
        val actual = sut.matches(request, Evaluators.context(), condition)

        // then
        assertTrue(actual)
        verify(exactly = 2) {
            segmentMatcher.matches(any(), any(), any())
        }
    }

    @Test
    fun `등록된 segment 중 일치하는게 있지만 MatchType 이 NOT_MATCH 면 false`() {
        // given
        val condition = condition {
            key(SEGMENT, "SEGMENT")
            match(NOT_MATCH, IN, STRING, "seg1", "seg2", "seg3")
        }

        val workspace = mockk<Workspace>()
        segment(workspace, "seg1", false)
        segment(workspace, "seg2", true)
        segment(workspace, "seg3", false)

        val request = experimentRequest(workspace)

        // when
        val actual = sut.matches(request, Evaluators.context(), condition)

        // then
        assertFalse(actual)
        verify(exactly = 2) {
            segmentMatcher.matches(any(), any(), any())
        }
    }

    private fun segment(workspace: Workspace, key: String, isMatch: Boolean): Segment {
        val segment = mockk<Segment>()
        every { segmentMatcher.matches(any(), any(), segment) } returns isMatch
        every { workspace.getSegmentOrNull(key) } returns segment
        return segment
    }
}