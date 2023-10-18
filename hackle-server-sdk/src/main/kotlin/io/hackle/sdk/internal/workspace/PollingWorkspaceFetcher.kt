package io.hackle.sdk.internal.workspace

import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.internal.scheduler.ScheduledJob
import io.hackle.sdk.core.internal.scheduler.Scheduler
import io.hackle.sdk.core.internal.utils.tryClose
import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.core.workspace.WorkspaceFetcher
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicReference

/**
 * @author Yong
 */
internal class PollingWorkspaceFetcher(
    private val httpWorkspaceFetcher: HttpWorkspaceFetcher,
    private val pollingIntervalMillis: Long,
    private val scheduler: Scheduler
) : WorkspaceFetcher, AutoCloseable {

    private val currentWorkspace = AtomicReference<Workspace>()
    private var pollingJob: ScheduledJob? = null

    override fun fetch(): Workspace? {
        return currentWorkspace.get()
    }

    private fun poll() {
        try {
            val workspace = httpWorkspaceFetcher.fetchIfModified() ?: return
            currentWorkspace.set(workspace)
        } catch (e: Exception) {
            log.error { "Failed to poll workspace: $e" }
        }
    }

    fun start() {
        if (pollingJob == null) {
            poll()
            pollingJob =
                scheduler.schedulePeriodically(pollingIntervalMillis, pollingIntervalMillis, MILLISECONDS) { poll() }
        }
    }

    override fun close() {
        pollingJob?.cancel()
        scheduler.tryClose()
    }

    companion object {
        private val log = Logger<PollingWorkspaceFetcher>()
    }
}
