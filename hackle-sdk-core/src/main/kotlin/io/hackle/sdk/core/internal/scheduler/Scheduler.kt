package io.hackle.sdk.core.internal.scheduler

import java.util.concurrent.TimeUnit

/**
 * @author Yong
 */
interface Scheduler {
    fun schedule(delay: Long, unit: TimeUnit, task: () -> Unit): ScheduledJob
    fun schedulePeriodically(delay: Long, period: Long, unit: TimeUnit, task: () -> Unit): ScheduledJob
}
