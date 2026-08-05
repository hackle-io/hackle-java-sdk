package io.hackle.sdk.core.evaluation

import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.RemoteConfigs
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains

internal class AbstractEvaluateRequestTest {

    @Test
    fun `equalsAndHashCode - entity 가 같으면 같은 request 로 판단한다`() {
        val request1 = Experiments.localRequest(experiment = Experiments.config(id = 1))
        val request11 = Experiments.localRequest(experiment = Experiments.config(id = 1))
        val request2 = Experiments.localRequest(experiment = Experiments.config(id = 2))

        assertTrue(request1 == request1)
        assertTrue(request1 == request11)
        assertTrue(request1 != request2)
        assertTrue(!request1.equals("request"))

        assertTrue(request1.hashCode() == request11.hashCode())
        assertTrue(request1.hashCode() != request2.hashCode())
    }

    @Test
    fun `equalsAndHashCode - entity 의 serviceType 이 다르면 다른 request 로 판단한다`() {
        val experimentRequest = Experiments.localRequest(experiment = Experiments.config(id = 1))
        val remoteConfigRequest = RemoteConfigs.localRequest(parameter = RemoteConfigs.config(id = 1))

        assertTrue(!experimentRequest.equals(remoteConfigRequest))
    }

    @Test
    fun `toString - entity 정보를 포함한다`() {
        val request = Experiments.localRequest(experiment = Experiments.config(id = 42))

        expectThat(request.toString())
            .contains("ExperimentLocalEvaluateRequest")
            .contains("id=42")
    }
}
