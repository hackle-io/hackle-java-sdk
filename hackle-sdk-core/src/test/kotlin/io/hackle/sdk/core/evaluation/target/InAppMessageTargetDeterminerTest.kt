package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.evaluator.inappmessage.InAppMessageRequest
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.model.Target
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo


@ExtendWith(MockKExtension::class)
internal class InAppMessageTargetDeterminerTest {

    @MockK
    private lateinit var targetMatcher: TargetMatcher

    @InjectMockKs
    private lateinit var sut: InAppMessageTargetDeterminer

    @Test
    fun `타겟팅 조건이 비어 있으면 true 를 반환한다`() {

        val request = mockk<InAppMessageRequest> {
            every { inAppMessage } returns mockk()
            every { inAppMessage.targetContext } returns targetContext(
                targets = emptyList()
            )
        }

        val actual = sut.determine(request, mockk())

        expectThat(actual) isEqualTo true

    }

    @Test
    fun `타겟팅 조건중 하나라도 맞으면 true 를 반환한다`() {

        val request = mockk<InAppMessageRequest> {
            every { inAppMessage } returns mockk()
            every { inAppMessage.targetContext } returns targetContext(
                targets = listOf(
                    target(true),
                    target(false),
                    target(false)
                )
            )
        }

        val actual = sut.determine(request, mockk())

        expectThat(actual) isEqualTo true

    }

    @Test
    fun `타겟팅 조건중 하나라도 맞으면 true 를 반환한다 2 `(){
        val request = mockk<InAppMessageRequest> {
            every { inAppMessage } returns mockk()
            every { inAppMessage.targetContext } returns targetContext(
                targets = listOf(
                    target(false),
                    target(true)
                )
            )
        }

        val actual = sut.determine(request, mockk())

        expectThat(actual) isEqualTo true

    }

    @Test
    fun `타겟팅 조건이 하나도 맞지 않으면 false 를 반환한다`() {
        val request = mockk<InAppMessageRequest> {
            every { inAppMessage } returns mockk()
            every { inAppMessage.targetContext } returns targetContext(
                targets = listOf(
                    target(false),
                    target(false),
                    target(false)
                )
            )
        }

        val actual = sut.determine(request, mockk())

        expectThat(actual) isEqualTo false
    }

    private fun targetContext(
        overrides: List<InAppMessage.TargetContext.UserOverride> = emptyList(),
        targets: List<Target>
    ): InAppMessage.TargetContext {
        return InAppMessage.TargetContext(targets, overrides)
    }

    private fun target(isMatch: Boolean): Target {
        val target = mockk<Target>()
        every { targetMatcher.matches(any(), any(), target) } returns isMatch
        return target
    }
}
