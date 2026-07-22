package io.hackle.sdk.core.evaluation.service.remoteconfig

import io.hackle.sdk.core.support.RemoteConfigs
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isSameInstanceAs

internal class RemoteConfigEvaluationTest {

    @Test
    fun `entity 와 result 로 평가를 생성한다`() {
        val parameter = RemoteConfigs.config()
        val evaluateResult = RemoteConfigs.result()

        val evaluation = RemoteConfigEvaluation(entity = parameter, result = evaluateResult)

        expectThat(evaluation) {
            get { entity } isSameInstanceAs parameter
            get { result } isSameInstanceAs evaluateResult
        }
    }
}
