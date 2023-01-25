package io.hackle.sdk.core.internal.metrics.flush

import io.hackle.sdk.core.internal.metrics.Counter
import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.metrics.cumulative.CumulativeCounter

internal class FlushCounter(id: Metric.Id) : AbstractFlushMetric<Counter>(id), Counter {

    override fun count(): Long {
        return current.count()
    }

    override fun increment(delta: Long) {
        current.increment(delta)
    }

    override fun initialMetric(): Counter {
        return CumulativeCounter(id)
    }
}
