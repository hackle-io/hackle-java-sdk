package io.hackle.sdk.core.workspace.evaluation.entity

import io.hackle.sdk.core.model.ServiceType
import io.hackle.sdk.core.support.InAppMessages
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

internal class InAppMessageEligibilityRemoteEvaluateResultTest {

    @Nested
    inner class ToEvaluationTest {

        @Test
        fun `자기 자신을 entity 와 result 로 하는 평가를 생성한다`() {
            val sut = InAppMessages.eligibilityRemoteResult()

            val evaluation = sut.toEvaluation()

            expectThat(evaluation) {
                get { entity } isSameInstanceAs sut
                get { result } isSameInstanceAs sut
            }
        }
    }

    @Nested
    inner class LayoutTest {

        @Test
        fun `함께 평가된 layout 결과를 유지한다`() {
            val layout = InAppMessages.layoutRemoteResult()

            val sut = InAppMessages.eligibilityRemoteResult(layout = layout)

            expectThat(sut) {
                get { this.layout } isSameInstanceAs layout
            }
        }
    }

    @Nested
    inner class ServiceTypeTest {

        @Test
        fun `IN_APP_MESSAGE`() {
            expectThat(InAppMessages.eligibilityRemoteResult()) {
                get { serviceType } isEqualTo ServiceType.IN_APP_MESSAGE
            }
        }
    }
}
