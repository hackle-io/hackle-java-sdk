package io.hackle.sdk.core.internal.scheduler

/**
 * @author Yong
 */
interface ScheduledJob {
    val isActive: Boolean
    val isCompleted: Boolean
    val isCancelled: Boolean
    fun cancel()
}
