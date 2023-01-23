package io.hackle.sdk.core.internal.metrics.flush

import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.metrics.Timer
import io.hackle.sdk.core.internal.metrics.cumulative.CumulativeTimer
import io.hackle.sdk.core.internal.time.Clock
import java.util.concurrent.TimeUnit

internal class FlushTimer(id: Metric.Id, private val clock: Clock) : AbstractFlushMetric<Timer>(id), Timer {

    override fun count(): Long {
        return current.count()
    }

    override fun totalTime(unit: TimeUnit): Double {
        return current.totalTime(unit)
    }

    override fun max(unit: TimeUnit): Double {
        return current.max(unit)
    }

    override fun record(amount: Long, unit: TimeUnit) {
        return current.record(amount, unit)
    }

    override fun <T> record(block: () -> T): T {
        return current.record(block)
    }

    override fun initialMetric(): Timer {
        return CumulativeTimer(id, clock)
    }
}
