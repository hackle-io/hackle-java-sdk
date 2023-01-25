package io.hackle.sdk.core.internal.metrics.logging

import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.metrics.flush.FlushMetricRegistry
import io.hackle.sdk.core.internal.scheduler.Scheduler
import io.hackle.sdk.core.internal.scheduler.Schedulers
import io.hackle.sdk.core.internal.time.Clock

class LoggingMetricRegistry(
    scheduler: Scheduler,
    flushIntervalMillis: Long,
    private val logging: (String) -> Unit,
    clock: Clock
) : FlushMetricRegistry(clock, scheduler, flushIntervalMillis) {

    init {
        start()
    }

    override fun flushMetric(metrics: List<Metric>) {
        metrics.asSequence()
            .sortedWith(COMPARATOR)
            .forEach(this::log)
    }

    private fun log(metric: Metric) {
        val metricLog = metric.measure()
            .joinToString(
                separator = " ",
                prefix = "${metric.id.name} ${metric.id.tags} "
            ) { "${it.field.tagKey}=${it.value}" }
        logging(metricLog)
    }

    class Builder(private val scheduler: Scheduler) {
        private var clock: Clock = Clock.SYSTEM
        private var flushIntervalMillis: Long = 60 * 1000
        private var logging: (String) -> Unit = { log.info { it } }

        fun clock(clock: Clock) = apply { this.clock = clock }
        fun flushIntervalMillis(flushIntervalMillis: Long) = apply { this.flushIntervalMillis = flushIntervalMillis }
        fun logging(logging: (String) -> Unit) = apply { this.logging = logging }

        fun build(): LoggingMetricRegistry {
            return LoggingMetricRegistry(scheduler, flushIntervalMillis, logging, clock)
        }
    }

    companion object {
        private val log = Logger<LoggingMetricRegistry>()

        private val COMPARATOR: Comparator<Metric> = compareBy(compareBy(Metric.Id::name, Metric.Id::type), Metric::id)
        fun builder(scheduler: Scheduler = Schedulers.executor("LoggingMetricRegistry-")): Builder {
            return Builder(scheduler)
        }
    }
}
