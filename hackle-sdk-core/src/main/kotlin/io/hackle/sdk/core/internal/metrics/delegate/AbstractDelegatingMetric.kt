package io.hackle.sdk.core.internal.metrics.delegate

import io.hackle.sdk.core.internal.metrics.AbstractMetric
import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.metrics.MetricRegistry

internal abstract class AbstractDelegatingMetric<M : Metric>(id: Metric.Id) : AbstractMetric(id), DelegatingMetric {

    private val lock = Any()

    private var _metrics = emptyMap<MetricRegistry, M>()
    protected val metrics: List<M> get() = _metrics.values.toList()

    protected abstract val noopMetric: M

    protected abstract fun registerNewMetric(registry: MetricRegistry): M

    final override fun add(registry: MetricRegistry) {
        val newMetric = registerNewMetric(registry)
        synchronized(lock) {
            val newMetrics = HashMap(_metrics)
            newMetrics[registry] = newMetric
            _metrics = newMetrics
        }
    }

    internal fun first(): M {
        return metrics.firstOrNull() ?: noopMetric
    }
}
