package io.hackle.sdk.core.internal.metrics.push

import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.internal.metrics.MetricRegistry
import io.hackle.sdk.core.internal.scheduler.ScheduledJob
import io.hackle.sdk.core.internal.scheduler.Scheduler
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.internal.utils.tryClose
import java.io.Closeable
import java.util.concurrent.TimeUnit.MILLISECONDS

abstract class PushMetricRegistry(
    clock: Clock,
    private val scheduler: Scheduler,
    private val pushIntervalMillis: Long,
) : MetricRegistry(clock), Closeable {

    private val log = Logger(javaClass.name)
    private val lock = Any()

    private var publishingJob: ScheduledJob? = null

    abstract fun publish()

    private fun safePublish() {
        try {
            publish()
        } catch (e: Throwable) {
            log.debug { "Unexpected exception while publishing metrics for [${javaClass.simpleName}]: $e" }
        }
    }

    fun start() {
        synchronized(lock) {
            if (publishingJob != null) {
                return
            }

            val delayMillis = pushIntervalMillis - (clock.currentMillis() % pushIntervalMillis) + 1;
            publishingJob =
                scheduler.schedulePeriodically(delayMillis, pushIntervalMillis, MILLISECONDS, this::safePublish)
            log.info { "${javaClass.simpleName} started. Publish metrics every ${pushIntervalMillis}ms." }
        }
    }

    fun stop() {
        synchronized(lock) {
            if (publishingJob == null) {
                return
            }

            publishingJob?.cancel()
            publishingJob = null
            safePublish()
            log.info { "${javaClass.simpleName} stopped." }
        }
    }

    override fun close() {
        stop()
        scheduler.tryClose()
    }
}
