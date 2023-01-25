package io.hackle.sdk.core.internal.metrics.noop

import io.hackle.sdk.core.internal.metrics.AbstractMetric
import io.hackle.sdk.core.internal.metrics.Counter
import io.hackle.sdk.core.internal.metrics.Metric

internal class NoopCounter(id: Metric.Id) : AbstractMetric(id), Counter {
    override fun count(): Long {
        return 0
    }

    override fun increment(delta: Long) {
    }
}