package io.hackle.sdk.core.decision

import io.hackle.sdk.common.decision.DecisionReason.IN_APP_MESSAGE_NOT_FOUND
import io.hackle.sdk.core.model.InAppMessages
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

internal class InAppMessageDecisionTest {

    @Test
    fun `isShow`() {
        expectThat(InAppMessages.decision(null, null, IN_APP_MESSAGE_NOT_FOUND).isShow).isFalse()
        expectThat(InAppMessages.decision(mockk(), null, IN_APP_MESSAGE_NOT_FOUND).isShow).isFalse()
        expectThat(InAppMessages.decision(mockk(), mockk(), IN_APP_MESSAGE_NOT_FOUND).isShow).isTrue()
    }
}
