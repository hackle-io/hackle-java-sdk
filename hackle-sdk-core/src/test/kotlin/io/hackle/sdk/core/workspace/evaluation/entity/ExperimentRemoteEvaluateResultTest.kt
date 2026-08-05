package io.hackle.sdk.core.workspace.evaluation.entity

import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.ServiceType
import io.hackle.sdk.core.support.Experiments
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

internal class ExperimentRemoteEvaluateResultTest {

    @Nested
    inner class ToEvaluationTest {

        @Test
        fun `자기 자신을 entity 와 result 로 하는 평가를 생성한다`() {
            val sut = Experiments.remoteResult()

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
        fun `experiment type 에 따라 결정된다`() {
            expectThat(Experiments.remoteResult(type = Experiment.Type.AB_TEST)) {
                get { serviceType } isEqualTo ServiceType.AB_TEST
            }
            expectThat(Experiments.remoteResult(type = Experiment.Type.FEATURE_FLAG)) {
                get { serviceType } isEqualTo ServiceType.FEATURE_FLAG
            }
        }
    }
}
