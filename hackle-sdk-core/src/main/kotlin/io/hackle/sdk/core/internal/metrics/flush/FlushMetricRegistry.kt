package io.hackle.sdk.core.internal.metrics.flush

import io.hackle.sdk.core.internal.metrics.Counter
import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.metrics.Timer
import io.hackle.sdk.core.internal.metrics.push.PushMetricRegistry
import io.hackle.sdk.core.internal.scheduler.Scheduler
import io.hackle.sdk.core.internal.time.Clock

abstract class FlushMetricRegistry(
    clock: Clock,
    scheduler: Scheduler,
    flushIntervalMillis: Long,
) : PushMetricRegistry(clock, scheduler, flushIntervalMillis) {

    final override fun createCounter(id: Metric.Id): Counter {
        return FlushCounter(id)
    }

    final override fun createTimer(id: Metric.Id): Timer {
        return FlushTimer(id, clock)
    }

    final override fun publish() {
        val metrics = metrics.asSequence()
            .filterIsInstance<FlushMetric<Metric>>()
            .map { it.flush() }
            .toList()
        flushMetric(metrics)
    }

    protected abstract fun flushMetric(metrics: List<Metric>)
}
