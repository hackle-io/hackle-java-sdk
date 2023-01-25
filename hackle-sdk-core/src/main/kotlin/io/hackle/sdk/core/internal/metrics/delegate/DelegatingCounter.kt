package io.hackle.sdk.core.internal.metrics.delegate

import io.hackle.sdk.core.internal.metrics.Counter
import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.metrics.MetricRegistry
import io.hackle.sdk.core.internal.metrics.noop.NoopCounter

internal class DelegatingCounter(id: Metric.Id) : AbstractDelegatingMetric<Counter>(id), Counter {

    override val noopMetric: Counter = NoopCounter(id)

    override fun count(): Long {
        return first().count()
    }

    override fun increment(delta: Long) {
        for (delegate in metrics) {
            delegate.increment(delta)
        }
    }

    override fun registerNewMetric(registry: MetricRegistry): Counter {
        return registry.counter(id)
    }
}
