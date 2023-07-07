package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.evaluator.inappmessage.InAppMessageRequest
import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.model.Target
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@ExtendWith(MockKExtension::class)
internal class InAppMessageUserOverrideDeterminerTest {

    @InjectMockKs
    private lateinit var sut: InAppMessageUserOverrideDeterminer

    @Test
    fun `유저 오버라이드 조건이 비어있으면 false 를 리턴한다`() {
        val request = mockk<InAppMessageRequest> {
            every { inAppMessage.targetContext } returns targetContext(
                overrides = emptyList()
            )
        }

        val actual = sut.determine(request)

        expectThat(actual) isEqualTo false
    }

    @Test
    fun `유저 오버라이드 조건에 하나라도 맞으면 true 를 리턴한다`() {
        val request = mockk<InAppMessageRequest> {
            every { inAppMessage.targetContext } returns targetContext(
                overrides = listOf(
                    userOverride(
                        "\$id",
                        "1", "2"
                    )
                )
            )
            every { user.identifiers["\$id"] } returns "1"
        }
        val actual = sut.determine(request)

        expectThat(actual) isEqualTo true

    }

    @Test
    fun `유저 오버라이드 조건의 identifier 타입이 맞지 않으면 false 를 리턴한다`() {
        val request = mockk<InAppMessageRequest> {
            every { inAppMessage.targetContext } returns targetContext(
                overrides = listOf(
                    userOverride(
                        "\$id",
                        "1", "2"
                    )
                )
            )
            every { user.identifiers["\$id"] } returns null
        }
        val actual = sut.determine(request)

        expectThat(actual) isEqualTo false
    }

    @Test
    fun `유저 오버라이드 조건이 값이 하나도 맞지 않으면 false 를 리턴한다`() {
        val request = mockk<InAppMessageRequest> {
            every { inAppMessage.targetContext } returns targetContext(
                overrides = listOf(
                    userOverride(
                        "\$id",
                        "1", "2"
                    )
                )
            )
            every { user.identifiers["\$id"] } returns "3"
        }
        val actual = sut.determine(request)

        expectThat(actual) isEqualTo false
    }

    private fun targetContext(
        overrides: List<InAppMessage.TargetContext.UserOverride>,
        targets: List<Target> = emptyList()
    ): InAppMessage.TargetContext {
        return InAppMessage.TargetContext(targets, overrides)
    }

    private fun userOverride(
        identifierType: String,
        vararg identifier: String
    ): InAppMessage.TargetContext.UserOverride {
        val identifiers = mutableListOf<String>()
        for (id in identifier) {
            identifiers.add(id)
        }

        return InAppMessage.TargetContext.UserOverride(
            identifierType,
            identifiers
        )
    }
}
