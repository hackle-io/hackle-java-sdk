package io.hackle.sdk.core.internal.metrics.cumulative

import io.hackle.sdk.core.internal.metrics.Counter
import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.metrics.MetricRegistry
import io.hackle.sdk.core.internal.metrics.Timer
import io.hackle.sdk.core.internal.time.Clock

class CumulativeMetricRegistry(clock: Clock = Clock.SYSTEM) : MetricRegistry(clock) {
    override fun createCounter(id: Metric.Id): Counter {
        return CumulativeCounter(id)
    }

    override fun createTimer(id: Metric.Id): Timer {
        return CumulativeTimer(id, clock)
    }
}
