package io.hackle.sdk.core.decision

import io.hackle.sdk.common.decision.DecisionReason.IN_APP_MESSAGE_NOT_FOUND
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

internal class InAppMessageDecisionTest {

    @Test
    fun `isShow`() {
        expectThat(InAppMessageDecision(null, null, IN_APP_MESSAGE_NOT_FOUND).isShow).isFalse()
        expectThat(InAppMessageDecision(mockk(), null, IN_APP_MESSAGE_NOT_FOUND).isShow).isFalse()
        expectThat(InAppMessageDecision(mockk(), mockk(), IN_APP_MESSAGE_NOT_FOUND).isShow).isTrue()
    }
}