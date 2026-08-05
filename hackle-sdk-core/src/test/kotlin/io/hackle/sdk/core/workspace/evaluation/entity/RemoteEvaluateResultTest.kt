package io.hackle.sdk.core.workspace.evaluation.entity

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEqualTo

internal class RemoteEvaluateResultTest {

    @Nested
    inner class KeyTest {

        @Test
        fun `type 과 id 로 생성한다`() {
            val key = RemoteEvaluateResult.Key(type = "AB_TEST", id = 42)

            expectThat(key) {
                get { type } isEqualTo "AB_TEST"
                get { id } isEqualTo 42L
            }
        }

        @Test
        fun `type 과 id 가 같으면 동등하다`() {
            val key = RemoteEvaluateResult.Key(type = "AB_TEST", id = 42)

            expectThat(key) {
                isEqualTo(RemoteEvaluateResult.Key(type = "AB_TEST", id = 42))
                get { hashCode() } isEqualTo RemoteEvaluateResult.Key(type = "AB_TEST", id = 42).hashCode()
            }
        }

        @Test
        fun `type 이나 id 가 다르면 동등하지 않다`() {
            val key = RemoteEvaluateResult.Key(type = "AB_TEST", id = 42)

            expectThat(key) {
                isNotEqualTo(RemoteEvaluateResult.Key(type = "FEATURE_FLAG", id = 42))
                isNotEqualTo(RemoteEvaluateResult.Key(type = "AB_TEST", id = 43))
            }
        }
    }
}
