package io.hackle.sdk.core.internal.metrics.push

import io.hackle.sdk.core.internal.metrics.Counter
import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.metrics.Timer
import io.hackle.sdk.core.internal.metrics.noop.NoopCounter
import io.hackle.sdk.core.internal.metrics.noop.NoopTimer
import io.hackle.sdk.core.internal.scheduler.ScheduledJob
import io.hackle.sdk.core.internal.scheduler.Scheduler
import io.hackle.sdk.core.internal.time.Clock
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.Closeable
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal class PushMetricRegistryTest {

    @Nested
    inner class StartTest {

        @Test
        fun `start scheduling`() {
            val scheduler = mockk<Scheduler>(relaxed = true)
            val sut = PushMetricRegistryStub(scheduler, 42)

            sut.start()

            verify(exactly = 1) {
                scheduler.schedulePeriodically(any(), 42, TimeUnit.MILLISECONDS, any())
            }
        }

        @Test
        fun `schedule only once`() {
            val scheduler = mockk<Scheduler>(relaxed = true)
            val sut = PushMetricRegistryStub(scheduler, 42)

            sut.start()
            sut.start()
            sut.start()

            verify(exactly = 1) {
                scheduler.schedulePeriodically(any(), any(), any(), any())
            }
        }
    }

    @Nested
    inner class StopTest {
        @Test
        fun `스케줄링을 취소해야함`() {
            val job = mockk<ScheduledJob>(relaxed = true)
            val scheduler = mockk<Scheduler> {
                every { schedulePeriodically(any(), any(), any(), any()) } returns job
            }
            val sut = PushMetricRegistryStub(scheduler, 42)

            sut.start()
            sut.stop()

            verify(exactly = 1) {
                job.cancel()
            }
        }

        @Test
        fun `publish 해야함`() {
            val job = mockk<ScheduledJob>(relaxed = true)
            val scheduler = mockk<Scheduler> {
                every { schedulePeriodically(any(), any(), any(), any()) } returns job
            }
            val sut = PushMetricRegistryStub(scheduler, 42)

            sut.start()
            sut.stop()

            expectThat(sut.publishCount.get()).isEqualTo(1)
        }

        @Test
        fun `시작되지 않은 경우`() {
            val sut = PushMetricRegistryStub(mockk(), 42)

            sut.stop()

            expectThat(sut.publishCount.get()).isEqualTo(0)
        }
    }

    @Nested
    inner class CloseTest {

        @Test
        fun `stop 시킨다`() {
            val sut = spyk(PushMetricRegistryStub(mockk(), 42))
            sut.close()
            verify(exactly = 1) {
                sut.stop()
            }
        }

        @Test
        fun `스케줄러를 닫는다`() {
            var closeCount = 0
            val scheduler = object : Scheduler, Closeable {
                override fun schedule(delay: Long, unit: TimeUnit, task: () -> Unit): ScheduledJob = mockk()
                override fun schedulePeriodically(
                    delay: Long,
                    period: Long,
                    unit: TimeUnit,
                    task: () -> Unit
                ): ScheduledJob = mockk()

                override fun close() {
                    closeCount++
                }
            }
            val sut = PushMetricRegistryStub(scheduler, 42)

            sut.close()

            expectThat(closeCount) isEqualTo 1
        }
    }

    private class PushMetricRegistryStub(
        scheduler: Scheduler,
        pushIntervalMillis: Long
    ) : PushMetricRegistry(Clock.SYSTEM, scheduler, pushIntervalMillis) {
        val publishCount = AtomicInteger()
        override fun createCounter(id: Metric.Id): Counter = NoopCounter(id)
        override fun createTimer(id: Metric.Id): Timer = NoopTimer(id)
        override fun publish() {
            publishCount.incrementAndGet()
        }
    }
}