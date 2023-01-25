package io.hackle.sdk.core.internal.scheduler

import io.hackle.sdk.core.internal.threads.NamedThreadFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * @author Yong
 */
object Schedulers {

    fun executor(executor: ScheduledExecutorService): Scheduler {
        return ExecutorScheduler(executor)
    }

    fun executor(namePrefix: String, corePoolSize: Int = 1, isDaemon: Boolean = true): Scheduler {
        val executor = Executors.newScheduledThreadPool(corePoolSize, NamedThreadFactory(namePrefix, isDaemon))
        return executor(executor)
    }

    private class ExecutorScheduler(private val executor: ScheduledExecutorService) : Scheduler, AutoCloseable {

        override fun schedule(delay: Long, unit: TimeUnit, task: () -> Unit): ScheduledJob {
            return Job(executor.schedule(task, delay, unit))
        }

        override fun schedulePeriodically(delay: Long, period: Long, unit: TimeUnit, task: () -> Unit): ScheduledJob {
            return Job(executor.scheduleAtFixedRate(task, delay, period, unit))
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
