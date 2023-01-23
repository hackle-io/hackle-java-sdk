package io.hackle.sdk.internal.monitoring.metrics

import io.hackle.sdk.core.internal.metrics.Metrics
import io.hackle.sdk.core.internal.metrics.Timer

object ApiCallMetrics {

    const val GET_WORKSPACE = "get.workspace"
    const val POST_EVENTS = "post.events"

    fun record(operation: String, sample: Timer.Sample, isSuccess: Boolean) {
        val tags = hashMapOf(
            "operation" to operation,
            "success" to isSuccess.toString()
        )
        val timer = Metrics.timer("api.call", tags)
        sample.stop(timer)
    }
}
