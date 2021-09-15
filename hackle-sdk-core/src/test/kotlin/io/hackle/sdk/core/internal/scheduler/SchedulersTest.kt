package io.hackle.sdk.core.internal.scheduler

import io.hackle.sdk.core.internal.utils.tryClose
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

internal class SchedulersTest {

    @Nested
    inner class ExecutorSchedulerTest {

        @Test
        fun `schedule`() {
            val executor = mockk<ScheduledExecutorService>(relaxed = true)
            val future = mockk<ScheduledFuture<*>>(relaxed = true)
            every { executor.schedule(any(), any(), any()) } returns future

            val scheduler = Schedulers.executor(executor)
            val task = mockk<() -> Unit>(relaxed = true)
            val job = scheduler.schedule(320, TimeUnit.MILLISECONDS, task)

            job.cancel()

            val realTask = slot<Runnable>()
            verify(exactly = 1) { executor.schedule(capture(realTask), 320, TimeUnit.MILLISECONDS) }

            realTask.captured.run()
            verify(exactly = 1) { task.invoke() }

            verify(exactly = 1) { future.cancel(false) }
        }

        @Test
        fun `schedulePeriodically`() {
            val executor = mockk<ScheduledExecutorService>(relaxed = true)
            val future = mockk<ScheduledFuture<*>>(relaxed = true)
            every { executor.scheduleAtFixedRate(any(), any(), any(), any()) } returns future

            val scheduler = Schedulers.executor(executor)
            val task = mockk<() -> Unit>(relaxed = true)
            val job = scheduler.schedulePeriodically(42, 320, TimeUnit.MILLISECONDS, task)

            job.cancel()

            val realTask = slot<Runnable>()
            verify(exactly = 1) { executor.scheduleAtFixedRate(capture(realTask), 42, 320, TimeUnit.MILLISECONDS) }

            realTask.captured.run()
            verify(exactly = 1) { task.invoke() }

            verify(exactly = 1) { future.cancel(false) }
        }

        @Test
        fun `close`() {
            val executor = mockk<ScheduledExecutorService>(relaxed = true)
            val scheduler = Schedulers.executor(executor)
            scheduler.tryClose()
            verify(exactly = 1) {
                executor.shutdownNow()
            }
        }

    }
}