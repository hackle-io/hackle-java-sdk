package io.hackle.sdk.core.internal.metrics.delegate

import io.hackle.sdk.core.internal.metrics.Counter
import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.metrics.MetricRegistry
import io.hackle.sdk.core.internal.metrics.Timer
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.internal.utils.tryClose
import java.io.Closeable

class DelegatingMetricRegistry(clock: Clock = Clock.SYSTEM) : MetricRegistry(clock), Closeable {

    private val registries = hashSetOf<MetricRegistry>()

    override fun createCounter(id: Metric.Id): Counter {
        return DelegatingCounter(id)
            .also { addRegistries(it) }
    }

    override fun createTimer(id: Metric.Id): Timer {
        return DelegatingTimer(id, clock)
            .also { addRegistries(it) }
    }

    private fun addRegistries(metric: DelegatingMetric) {
        lock {
            for (registry in registries) {
                metric.add(registry)
            }
        }
    }

    fun add(registry: MetricRegistry) {
        if (registry is DelegatingMetricRegistry) {
            return
        }
        lock {
            if (registries.add(registry)) {
                for (metric in metrics) {
                    if (metric is DelegatingMetric) {
                        metric.add(registry)
                    }
                }
            }
        }
    }

    override fun close() {
        registries.forEach { it.tryClose() }
    }

    override fun toString(): String {
        return "DelegatingMetricRegistry(${registries.map { it.javaClass.simpleName }})"
    }
}
