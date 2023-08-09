package io.hackle.sdk.core.internal.metrics.cumulative

import io.hackle.sdk.core.internal.metrics.MetricField.*
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isIn
import strikt.assertions.withElementAt
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class CumulativeTimerTest {

    @Test
    fun `negative records should ignored`() {
        val timer = CumulativeMetricRegistry().timer("timer")
        timer.record(-1, TimeUnit.NANOSECONDS)
        expectThat(timer.count()).isEqualTo(0)
    }

    @Test
    fun `concurrency record`() {
        val timer = CumulativeMetricRegistry().timer("timer")

        val executor = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(10000)
        repeat(10000) {

            executor.execute {
                timer.record((it + 1).toLong(), TimeUnit.NANOSECONDS)
                latch.countDown()
            }
        }

        latch.await()

        expectThat(timer.count()).isEqualTo(10000)
        expectThat(timer.totalTime(TimeUnit.NANOSECONDS)).isEqualTo(50005000.0)
        expectThat(timer.max(TimeUnit.NANOSECONDS)).isEqualTo(10000.0)
        expectThat(timer.mean(TimeUnit.NANOSECONDS)).isEqualTo(5000.5)
    }

    @Test
    fun `lambda record`() {

        val timer = CumulativeMetricRegistry().timer("timer")

        repeat(10) {
            timer.record {
                Thread.sleep(100)
            }
        }

        expectThat(timer.count()).isEqualTo(10)
        expectThat(timer.totalTime(TimeUnit.MILLISECONDS)).isIn(1000.0..1050.0)
        expectThat(timer.max(TimeUnit.MILLISECONDS)).isIn(100.0..120.0)
        expectThat(timer.mean(TimeUnit.MILLISECONDS)).isIn(100.0..120.0)
    }

    @Test
    fun `measure`() {
        val timer = CumulativeMetricRegistry().timer("timer")
        val measurements = timer.measure()

        expectThat(measurements).hasSize(4)
            .withElementAt(0) {
                get { field } isEqualTo COUNT
                get { value } isEqualTo 0.0
            }
            .withElementAt(1) {
                get { field } isEqualTo TOTAL
                get { value } isEqualTo 0.0
            }
            .withElementAt(2) {
                get { field } isEqualTo MAX
                get { value } isEqualTo 0.0
            }
            .withElementAt(3) {
                get { field } isEqualTo MEAN
                get { value } isEqualTo 0.0
            }
        timer.record(42, TimeUnit.MILLISECONDS)
        expectThat(measurements).hasSize(4)
            .withElementAt(0) {
                get { field } isEqualTo COUNT
                get { value } isEqualTo 1.0
            }
            .withElementAt(1) {
                get { field } isEqualTo TOTAL
                get { value } isEqualTo 42.0
            }
            .withElementAt(2) {
                get { field } isEqualTo MAX
                get { value } isEqualTo 42.0
            }
            .withElementAt(3) {
                get { field } isEqualTo MEAN
                get { value } isEqualTo 42.0
            }
    }
}
