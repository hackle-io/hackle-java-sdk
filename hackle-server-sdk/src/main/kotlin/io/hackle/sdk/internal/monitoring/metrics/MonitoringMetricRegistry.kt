package io.hackle.sdk.internal.monitoring.metrics

import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.internal.metrics.Counter
import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.metrics.Timer
import io.hackle.sdk.core.internal.metrics.flush.FlushMetricRegistry
import io.hackle.sdk.core.internal.scheduler.Scheduler
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.internal.http.isSuccessful
import io.hackle.sdk.internal.utils.toJson
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import java.net.URI

internal class MonitoringMetricRegistry(
    monitoringBaseUrl: String,
    scheduler: Scheduler,
    flushIntervalMillis: Long,
    private val httpClient: CloseableHttpClient,
    clock: Clock = Clock.SYSTEM,
) : FlushMetricRegistry(clock, scheduler, flushIntervalMillis) {

    private val monitoringEndpoint = URI("$monitoringBaseUrl/metrics")

    init {
        start()
    }

    override fun flushMetric(metrics: List<Metric>) {
        metrics.asSequence()
            .filter(this::isDispatchTarget)
            .chunked(500)
            .forEach(this::dispatch)
    }

    // Dispatch only measured metrics
    private fun isDispatchTarget(metric: Metric): Boolean {
        val count = when (metric.id.type) {
            Metric.Type.COUNTER -> (metric as? Counter)?.count() ?: return false
            Metric.Type.TIMER -> (metric as? Timer)?.count() ?: return false
        }
        return count > 0
    }

    private fun dispatch(metrics: List<Metric>) {
        val batch = MetricBatchDto(metrics.map { metric(it) })
        val post = HttpPost(monitoringEndpoint).apply {
            entity = StringEntity(batch.toJson(), REQUEST_CONTENT_TYPE)
        }

        httpClient.execute(post).use { response ->
            if (!response.isSuccessful) {
                log.warn { "Failed to flushing metrics" }
            }
        }
    }

    private fun metric(metric: Metric): MetricDto {
        return MetricDto(
            name = metric.id.name,
            tags = metric.id.tags,
            type = metric.id.type.name,
            measurements = metric.measure().associate { it.field.tagKey to it.value }
        )
    }

    companion object {
        private val log = Logger<MonitoringMetricRegistry>()
        private val REQUEST_CONTENT_TYPE: ContentType = ContentType.create("application/json", "utf-8")
    }
}
