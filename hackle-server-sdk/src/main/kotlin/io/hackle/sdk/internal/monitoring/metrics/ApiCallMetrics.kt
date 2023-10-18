package io.hackle.sdk.internal.monitoring.metrics

import io.hackle.sdk.core.internal.metrics.Metrics
import io.hackle.sdk.core.internal.metrics.Timer
import io.hackle.sdk.internal.http.isNotModified
import io.hackle.sdk.internal.http.isSuccessful
import org.apache.http.client.methods.CloseableHttpResponse

internal object ApiCallMetrics {

    const val GET_WORKSPACE = "get.workspace"
    const val POST_EVENTS = "post.events"

    inline fun record(operation: String, call: () -> CloseableHttpResponse): CloseableHttpResponse {
        val sample = Timer.start()
        return try {
            call().also { record(operation, sample, it) }
        } catch (e: Throwable) {
            record(operation, sample, null)
            throw e
        }
    }

    fun record(operation: String, sample: Timer.Sample, response: CloseableHttpResponse?) {
        val tags = hashMapOf(
            "operation" to operation,
            "success" to success(response),
        )
        val timer = Metrics.timer("api.call", tags)
        sample.stop(timer)
    }

    private fun success(response: CloseableHttpResponse?): String {
        if (response == null) {
            return "false"
        }
        val success = response.isSuccessful || response.isNotModified
        return success.toString()
    }
}
