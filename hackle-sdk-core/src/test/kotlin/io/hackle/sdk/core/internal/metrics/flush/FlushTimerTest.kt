package io.hackle.sdk.core.internal.metrics.flush

import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.metrics.MetricField
import io.hackle.sdk.core.internal.metrics.cumulative.CumulativeTimer
import io.hackle.sdk.core.internal.time.Clock
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.withElementAt
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class FlushTimerTest {

    @Test
    fun `record only`() {
        val timer = timer()
        repeat(100) {
            timer.record((it + 1).toLong(), TimeUnit.NANOSECONDS)
        }
        expectThat(timer.count()).isEqualTo(100)
        expectThat(timer.totalTime(TimeUnit.NANOSECONDS)).isEqualTo(5050.0)
        expectThat(timer.max(TimeUnit.NANOSECONDS)).isEqualTo(100.0)
        expectThat(timer.mean(TimeUnit.NANOSECONDS)).isEqualTo(50.5)
    }

    @Test
    fun `lambda record`() {
        val clock = mockk<Clock> {
            every { tick() } returnsMany listOf(100, 142)
        }
        val timer = timer(clock)
        val actual = timer.record { "42" }
        expectThat(actual).isEqualTo("42")
        expectThat(timer.totalTime(TimeUnit.NANOSECONDS)).isEqualTo(42.0)
    }

    @Test
    fun `record with flush`() {
        val timer = timer()
        repeat(100) {
            timer.record((it + 1).toLong(), TimeUnit.NANOSECONDS)
        }
        expectThat(timer.count()).isEqualTo(100)
        expectThat(timer.totalTime(TimeUnit.NANOSECONDS)).isEqualTo(5050.0)
        expectThat(timer.max(TimeUnit.NANOSECONDS)).isEqualTo(100.0)
        expectThat(timer.mean(TimeUnit.NANOSECONDS)).isEqualTo(50.5)

        timer.flush()
        expectThat(timer.count()).isEqualTo(0)
        expectThat(timer.totalTime(TimeUnit.NANOSECONDS)).isEqualTo(0.0)
        expectThat(timer.max(TimeUnit.NANOSECONDS)).isEqualTo(0.0)
        expectThat(timer.mean(TimeUnit.NANOSECONDS)).isEqualTo(0.0)
    }

    @Test
    fun `concurrency record`() {

        repeat(100) {
            val timer = timer()
            val latch = CountDownLatch(10000)
            val jobs = List(10000) {
                CompletableFuture.supplyAsync {
                    if (it % 2 == 0) {
                        timer.apply { record((it + 1).toLong(), TimeUnit.NANOSECONDS) }
                    } else {
                        timer.flush()
                    }.also {
                        latch.countDown()
                    }
                }
            }
            latch.await()

            val finalTimers = jobs.asSequence()
                .map { it.join() }
                .filterIsInstance<CumulativeTimer>()
                .toList() + timer.flush()

            expectThat(finalTimers.sumOf { it.count() }).isEqualTo(5000)
            expectThat(finalTimers.sumOf { it.totalTime(TimeUnit.NANOSECONDS) }).isEqualTo(25000000.0)
        }
    }

    @Test
    fun `measure`() {
        val timer = timer()
        timer.record(42, TimeUnit.MILLISECONDS)

        val measurements = timer.measure()
        expectThat(measurements).hasSize(4)
            .withElementAt(0) {
                get { field } isEqualTo MetricField.COUNT
                get { value } isEqualTo 1.0
            }
            .withElementAt(1) {
                get { field } isEqualTo MetricField.TOTAL
                get { value } isEqualTo 42.0
            }
            .withElementAt(2) {
                get { field } isEqualTo MetricField.MAX
                get { value } isEqualTo 42.0
            }
            .withElementAt(3) {
                get { field } isEqualTo MetricField.MEAN
                get { value } isEqualTo 42.0
            }

        timer.flush()

        expectThat(measurements).hasSize(4)
            .withElementAt(0) {
                get { field } isEqualTo MetricField.COUNT
                get { value } isEqualTo 0.0
            }
            .withElementAt(1) {
                get { field } isEqualTo MetricField.TOTAL
                get { value } isEqualTo 0.0
            }
            .withElementAt(2) {
                get { field } isEqualTo MetricField.MAX
                get { value } isEqualTo 0.0
            }
            .withElementAt(3) {
                get { field } isEqualTo MetricField.MEAN
                get { value } isEqualTo 0.0
            }
    }

    private fun timer(clock: Clock = Clock.SYSTEM): FlushTimer {
        return FlushTimer(Metric.Id("timer", emptyMap(), Metric.Type.TIMER), clock)
    }
}