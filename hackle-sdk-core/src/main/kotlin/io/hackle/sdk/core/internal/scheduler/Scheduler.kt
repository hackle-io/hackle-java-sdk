package io.hackle.sdk.core.internal.scheduler

import java.time.Duration

/**
 * @author Yong
 */
interface Scheduler {
    fun schedule(delay: Duration, task: () -> Unit): ScheduledJob
    fun schedulePeriodically(delay: Duration, period: Duration, task: () -> Unit): ScheduledJob
}
