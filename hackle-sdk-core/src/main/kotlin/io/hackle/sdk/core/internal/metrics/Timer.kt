package io.hackle.sdk.core.internal.metrics

import io.hackle.sdk.core.internal.metrics.Metric.Type.TIMER
import io.hackle.sdk.core.internal.time.Clock
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS

interface Timer : Metric {

    fun count(): Long

    fun totalTime(unit: TimeUnit): Double

    fun max(unit: TimeUnit): Double

    fun mean(unit: TimeUnit): Double {
        val count = count()
        return if (count == 0L) 0.0 else totalTime(unit) / count
    }

    fun record(amount: Long, unit: TimeUnit)

    fun <T> record(block: () -> T): T

    override fun measure(): List<Measurement> {
        return listOf(
            Measurement(MetricField.COUNT) { count() },
            Measurement(MetricField.TOTAL) { totalTime(MILLISECONDS) },
            Measurement(MetricField.MAX) { max(MILLISECONDS) },
            Measurement(MetricField.MEAN) { mean(MILLISECONDS) }
        )
    }

    class Sample(private val clock: Clock) {

        private val startTick: Long = clock.tick()

        fun stop(timer: Timer): Long {
            val durationNanos = clock.tick() - startTick
            timer.record(durationNanos, TimeUnit.NANOSECONDS)
            return durationNanos
        }
    }

    private class Builder(name: String) : Metric.Builder<Timer>(name, TIMER, MetricRegistry::timer)

    companion object {

        fun builder(name: String): Metric.Builder<Timer> {
            return Builder(name)
        }

        fun start(clock: Clock = Clock.SYSTEM): Sample {
            return Sample(clock)
        }
    }
}
