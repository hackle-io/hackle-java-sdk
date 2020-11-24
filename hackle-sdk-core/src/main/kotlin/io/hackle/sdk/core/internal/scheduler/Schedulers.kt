package io.hackle.sdk.core.internal.scheduler

import io.hackle.sdk.core.internal.utils.millis
import java.time.Duration
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * @author Yong
 */
object Schedulers {

    fun executor(executor: ScheduledExecutorService): Scheduler {
        return ExecutorScheduler(executor)
    }

    private class ExecutorScheduler(private val executor: ScheduledExecutorService) : Scheduler, AutoCloseable {

        override fun schedule(delay: Duration, task: () -> Unit): ScheduledJob {
            return Job(executor.schedule(task, delay.millis, MILLISECONDS))
        }

        override fun schedulePeriodically(delay: Duration, period: Duration, task: () -> Unit): ScheduledJob {
            return Job(executor.scheduleAtFixedRate(task, delay.millis, period.millis, MILLISECONDS))
        }

        override fun close() {
            executor.shutdownNow()
        }

        private class Job(private val real: ScheduledFuture<*>) : ScheduledJob {
            override fun cancel() {
                real.cancel(false)
            }
        }
    }
}
