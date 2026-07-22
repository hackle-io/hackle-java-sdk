package io.hackle.sdk.core.evaluation.service.remoteconfig

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.model.ValueType.*
import io.hackle.sdk.core.support.RemoteConfigs
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

internal class RemoteConfigEvaluateResultTest {

    @Test
    fun `of - reason 과 value 로 생성한다`() {
        val value = RemoteConfigs.value()

        val result = RemoteConfigEvaluateResult.of(DecisionReason.TARGET_RULE_MATCH, value)

        expectThat(result) {
            get { reason } isEqualTo DecisionReason.TARGET_RULE_MATCH
            get { this.value } isSameInstanceAs value
        }
    }

    @Test
    fun `of - value 가 null 이면 reason 그대로 생성한다`() {
        val request = RemoteConfigs.localRequest()

        val result = RemoteConfigEvaluateResult.of(request, null, DecisionReason.IDENTIFIER_NOT_FOUND)

        expectThat(result) {
            get { reason } isEqualTo DecisionReason.IDENTIFIER_NOT_FOUND
            get { value }.isNull()
        }
    }

    @Test
    fun `of - requiredType 과 value 타입이 일치하는지 확인한다`() {
        verifyTypeMatch(STRING, "match_string", true)
        verifyTypeMatch(STRING, "", true)
        verifyTypeMatch(STRING, 0, false)
        verifyTypeMatch(STRING, 1, false)
        verifyTypeMatch(STRING, false, false)
        verifyTypeMatch(STRING, true, false)

        verifyTypeMatch(NUMBER, 0, true)
        verifyTypeMatch(NUMBER, 1, true)
        verifyTypeMatch(NUMBER, -1, true)
        verifyTypeMatch(NUMBER, 0L, true)
        verifyTypeMatch(NUMBER, 1L, true)
        verifyTypeMatch(NUMBER, -1L, true)
        verifyTypeMatch(NUMBER, 0.0, true)
        verifyTypeMatch(NUMBER, 1.0, true)
        verifyTypeMatch(NUMBER, -1.0, true)
        verifyTypeMatch(NUMBER, 1.1, true)
        verifyTypeMatch(NUMBER, "1", false)
        verifyTypeMatch(NUMBER, "0", false)
        verifyTypeMatch(NUMBER, true, false)
        verifyTypeMatch(NUMBER, false, false)

        verifyTypeMatch(BOOLEAN, true, true)
        verifyTypeMatch(BOOLEAN, false, true)
        verifyTypeMatch(BOOLEAN, 0, false)
        verifyTypeMatch(BOOLEAN, 1, false)

        verifyTypeMatch(VERSION, "1.0.0", false)
        verifyTypeMatch(JSON, "{}", false)
    }

    private fun verifyTypeMatch(requiredType: ValueType, rawValue: Any, isMatch: Boolean) {
        val request = RemoteConfigs.localRequest(requiredType = requiredType)
        val value = RemoteConfigs.value(rawValue = rawValue)

        val actual = RemoteConfigEvaluateResult.of(request, value, DecisionReason.DEFAULT_RULE)

        if (isMatch) {
            expectThat(actual) {
                get { reason } isEqualTo DecisionReason.DEFAULT_RULE
                get { this.value } isSameInstanceAs value
            }
        } else {
            expectThat(actual) {
                get { reason } isEqualTo DecisionReason.TYPE_MISMATCH
                get { this.value } isSameInstanceAs value
            }
        }
    }
}
