package io.hackle.sdk.core.workspace.evaluation.entity

import io.hackle.sdk.core.model.ServiceType
import io.hackle.sdk.core.support.RemoteConfigs
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

internal class RemoteConfigParameterRemoteEvaluateResultTest {

    @Nested
    inner class ToEvaluationTest {

        @Test
        fun `자기 자신을 entity 와 result 로 하는 평가를 생성한다`() {
            val sut = RemoteConfigs.remoteResult()

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
        fun `REMOTE_CONFIG`() {
            expectThat(RemoteConfigs.remoteResult()) {
                get { serviceType } isEqualTo ServiceType.REMOTE_CONFIG
            }
        }
    }
}
