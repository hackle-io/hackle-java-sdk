package io.hackle.sdk.core.internal.metrics.delegate

import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.metrics.MetricRegistry
import io.hackle.sdk.core.internal.metrics.Timer
import io.hackle.sdk.core.internal.metrics.noop.NoopTimer
import io.hackle.sdk.core.internal.time.Clock
import java.util.concurrent.TimeUnit

internal class DelegatingTimer(id: Metric.Id, private val clock: Clock) : AbstractDelegatingMetric<Timer>(id), Timer {

    override val noopMetric: Timer = NoopTimer(id)

    override fun count(): Long {
        return first().count()
    }

    override fun totalTime(unit: TimeUnit): Double {
        return first().totalTime(unit)
    }

    override fun max(unit: TimeUnit): Double {
        return first().max(unit)
    }

    override fun record(amount: Long, unit: TimeUnit) {
        for (metric in metrics) {
            metric.record(amount, unit)
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

    override fun registerNewMetric(registry: MetricRegistry): Timer {
        return registry.timer(id)
    }
}
