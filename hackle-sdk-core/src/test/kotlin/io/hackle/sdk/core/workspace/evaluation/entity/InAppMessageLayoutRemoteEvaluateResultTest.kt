package io.hackle.sdk.core.workspace.evaluation.entity

import io.hackle.sdk.core.model.ServiceType
import io.hackle.sdk.core.support.InAppMessages
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

internal class InAppMessageLayoutRemoteEvaluateResultTest {

    @Nested
    inner class ToEvaluationTest {

        @Test
        fun `자기 자신을 entity 와 result 로 하는 평가를 생성한다`() {
            val sut = InAppMessages.layoutRemoteResult()

            val evaluation = sut.toEvaluation()

            expectThat(evaluation) {
                get { entity } isSameInstanceAs sut
                get { result } isSameInstanceAs sut
            }
        }
    }

    @Nested
    inner class ServiceTypeTest {

        @Test
        fun `IN_APP_MESSAGE`() {
            expectThat(InAppMessages.layoutRemoteResult()) {
                get { serviceType } isEqualTo ServiceType.IN_APP_MESSAGE
            }
        }
    }
}
