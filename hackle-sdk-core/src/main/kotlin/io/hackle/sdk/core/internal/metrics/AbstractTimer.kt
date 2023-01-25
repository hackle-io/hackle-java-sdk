package io.hackle.sdk.core.internal.metrics

import io.hackle.sdk.core.internal.time.Clock
import java.util.concurrent.TimeUnit

abstract class AbstractTimer(id: Metric.Id, protected val clock: Clock) : AbstractMetric(id), Timer {
    final override fun record(amount: Long, unit: TimeUnit) {
        if (amount >= 0) {
            doRecord(amount, unit)
        }
    }

    override fun <T> record(block: () -> T): T {
        val s = clock.tick()
        return try {
            block()
        } finally {
            val e = clock.tick()
            record(e - s, TimeUnit.NANOSECONDS)
        }
    }

    protected abstract fun doRecord(amount: Long, unit: TimeUnit)
}
