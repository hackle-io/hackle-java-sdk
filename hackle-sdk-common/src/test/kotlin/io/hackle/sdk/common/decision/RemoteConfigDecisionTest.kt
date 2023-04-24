package io.hackle.sdk.common.decision

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class RemoteConfigDecisionTest {
    @Test
    fun `decision`() {
        expectThat(RemoteConfigDecision.of("hello", DecisionReason.DEFAULT_RULE)) {
            get { value } isEqualTo "hello"
            get { reason } isEqualTo DecisionReason.DEFAULT_RULE
        }
    }
}