package io.hackle.sdk.core.internal.metrics.noop

import io.hackle.sdk.core.internal.metrics.AbstractMetric
import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.metrics.Timer
import java.util.concurrent.TimeUnit

internal class NoopTimer(id: Metric.Id) : AbstractMetric(id), Timer {

    override fun count(): Long {
        return 0
    }

    override fun totalTime(unit: TimeUnit): Double {
        return 0.0
    }

    override fun max(unit: TimeUnit): Double {
        return 0.0
    }

    override fun record(amount: Long, unit: TimeUnit) {
    }

    override fun <T> record(block: () -> T): T {
        return block()
    }
}
