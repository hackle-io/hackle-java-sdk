package io.hackle.sdk.internal.event

import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.internal.metrics.Timer
import io.hackle.sdk.internal.monitoring.metrics.ApiCallMetrics
import io.hackle.sdk.internal.monitoring.metrics.ApiCallMetrics.POST_EVENTS
import io.hackle.sdk.internal.http.isSuccessful
import io.hackle.sdk.internal.http.statusCode
import io.hackle.sdk.internal.utils.toJson
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import java.net.URI
import java.util.concurrent.ExecutorService
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * @author Yong
 */
internal class EventDispatcher(
    eventBaseUrl: String,
    private val httpClient: CloseableHttpClient,
    private val dispatcherExecutor: ExecutorService,
    private val shutdownTimeoutMillis: Long
) : AutoCloseable {

    private val eventEndpoint = URI("$eventBaseUrl/api/v2/events")

    fun dispatch(userEvents: List<UserEvent>) {
        val task = DispatchTask(userEvents.toPayload())

        try {
            dispatcherExecutor.submit(task)
        } catch (e: RejectedExecutionException) {
            log.warn { "Exceeded dispatcher queue capacity. Events dispatch rejected." }
        } catch (e: Exception) {
            log.error { "Unexpected exception while submitting events for dispatch." }
        }
    }

    override fun close() {
        dispatcherExecutor.shutdown()

        try {
            if (!dispatcherExecutor.awaitTermination(shutdownTimeoutMillis, MILLISECONDS)) {
                log.warn { "Failed to dispatch previously submitted events" }
                dispatcherExecutor.shutdownNow()
            }
        } catch (e: Exception) {
            log.error { "Failed to gracefully shutdown AsyncEventDispatcher" }
            dispatcherExecutor.shutdownNow()
        }
    }

    inner class DispatchTask(private val payload: EventPayloadDto) : Runnable {
        override fun run() {
            val sample = Timer.start()
            try {
                dispatch()
                ApiCallMetrics.record(POST_EVENTS, sample, true)
            } catch (e: Exception) {
                log.error { "Failed to dispatch events: $e" }
                ApiCallMetrics.record(POST_EVENTS, sample, false)
            }
        }

        private fun dispatch() {
            val post = HttpPost(eventEndpoint).apply {
                entity = StringEntity(payload.toJson(), REQUEST_CONTENT_TYPE)
            }

            httpClient.execute(post).use { response ->
                check(response.isSuccessful) { "Http status code: ${response.statusCode}" }
            }
        }
    }

    companion object {
        private val log = Logger<EventDispatcher>()
        private val REQUEST_CONTENT_TYPE: ContentType = ContentType.create("application/json", "utf-8")
    }
}
