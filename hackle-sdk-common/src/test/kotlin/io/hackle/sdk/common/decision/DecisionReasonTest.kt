package io.hackle.sdk.common.decision

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class DecisionReasonTest {

    @Test
    fun `from - 이름이 일치하는 DecisionReason 을 반환한다`() {
        expectThat(DecisionReason.from("TRAFFIC_ALLOCATED")) isEqualTo DecisionReason.TRAFFIC_ALLOCATED
    }

    @Test
    fun `from - 일치하는 이름이 없으면 UNKNOWN 을 반환한다`() {
        expectThat(DecisionReason.from("NOT_EXIST_REASON")) isEqualTo DecisionReason.UNKNOWN
    }
}
