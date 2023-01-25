package io.hackle.sdk.core.internal.metrics.cumulative

import io.hackle.sdk.core.internal.metrics.AbstractTimer
import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.internal.time.convert
import io.hackle.sdk.core.internal.time.nanosToUnit
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.NANOSECONDS
import java.util.concurrent.atomic.AtomicLong

class CumulativeTimer(id: Metric.Id, clock: Clock) : AbstractTimer(id, clock) {

    private val count = AtomicLong()
    private val total = AtomicLong()
    private val max = AtomicLong()

    override fun doRecord(amount: Long, unit: TimeUnit) {
        val nanos = NANOSECONDS.convert(amount.toDouble(), unit).toLong()
        count.getAndAdd(1)
        total.getAndAdd(nanos)
        updateMax(nanos)
    }

    // For support below Android 24 (Do NOT use getAndAccumulate )
    private fun updateMax(nanos: Long) {
        var currentMax: Long
        do {
            currentMax = max.get()
        } while (currentMax < nanos && !max.compareAndSet(currentMax, nanos))
    }

    override fun count(): Long {
        return count.get()
    }

    override fun totalTime(unit: TimeUnit): Double {
        return nanosToUnit(total.get().toDouble(), unit)
    }

    override fun max(unit: TimeUnit): Double {
        return nanosToUnit(max.get().toDouble(), unit)
    }
}
