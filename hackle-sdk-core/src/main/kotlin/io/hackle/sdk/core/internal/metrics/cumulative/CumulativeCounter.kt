package io.hackle.sdk.core.internal.metrics.cumulative

import io.hackle.sdk.core.internal.metrics.AbstractMetric
import io.hackle.sdk.core.internal.metrics.Counter
import io.hackle.sdk.core.internal.metrics.Metric
import java.util.concurrent.atomic.AtomicLong

class CumulativeCounter(id: Metric.Id) : AbstractMetric(id), Counter {

    private val value = AtomicLong()

    override fun count(): Long {
        return value.get()
    }

    override fun increment(delta: Long) {
        value.getAndAdd(delta)
    }
}
