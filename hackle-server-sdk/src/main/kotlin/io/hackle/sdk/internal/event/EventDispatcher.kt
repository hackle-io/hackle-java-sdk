package io.hackle.sdk.internal.event

import io.hackle.sdk.HackleConfig
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.internal.http.isSuccessful
import io.hackle.sdk.internal.http.statusCode
import io.hackle.sdk.internal.monitoring.metrics.ApiCallMetrics
import io.hackle.sdk.internal.monitoring.metrics.ApiCallMetrics.POST_EVENTS
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
    config: HackleConfig,
    private val httpClient: CloseableHttpClient,
    private val dispatcherExecutor: ExecutorService,
    private val shutdownTimeoutMillis: Long
) : AutoCloseable {

    private val eventEndpoint = URI(url(config))

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
            try {
                dispatch()
            } catch (e: Exception) {
                log.error { "Failed to dispatch events: $e" }
            }
        }

        private fun dispatch() {
            val post = HttpPost(eventEndpoint).apply {
                entity = StringEntity(payload.toJson(), REQUEST_CONTENT_TYPE)
            }

            val response = ApiCallMetrics.record(POST_EVENTS) {
                httpClient.execute(post)
            }

            response.use {
                check(it.isSuccessful) { "Http status code: ${response.statusCode}" }
            }
        }
    }

    companion object {
        private val log = Logger<EventDispatcher>()
        private val REQUEST_CONTENT_TYPE: ContentType = ContentType.create("application/json", "utf-8")

        private fun url(config: HackleConfig): String {
            return "${config.eventUrl}/api/v2/events"
        }
    }
}
