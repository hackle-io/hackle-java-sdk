package io.hackle.sdk.core.evaluation.mode.remote

import io.hackle.sdk.core.evaluation.EvaluationPhase
import io.hackle.sdk.core.support.Experiments
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class RemoteEvaluateRequestTest {

    @Test
    fun `phase 는 항상 RUNTIME 이다`() {
        val request = Experiments.remoteRequest()

        expectThat(request.phase) isEqualTo EvaluationPhase.RUNTIME
    }
}
