package io.hackle.sdk.internal.workspace

import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.internal.scheduler.ScheduledJob
import io.hackle.sdk.core.internal.scheduler.Scheduler
import io.hackle.sdk.core.internal.utils.tryClose
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.core.workspace.config.WorkspaceConfig
import io.hackle.sdk.core.workspace.config.WorkspaceConfigFetcher
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicReference

/**
 * @author Yong
 */
internal class PollingWorkspaceConfigFetcher(
    private val fetcher: HttpWorkspaceConfigFetcher,
    private val pollingIntervalMillis: Long,
    private val scheduler: Scheduler,
) : WorkspaceConfigFetcher, AutoCloseable {

    private val currentWorkspace = AtomicReference<WorkspaceConfig?>()
    private var pollingJob: ScheduledJob? = null

    override fun metadata(): Workspace.Metadata? {
        return currentWorkspace.get()?.metadata
    }

    override fun workspace(user: HackleUser): WorkspaceConfig? {
        return currentWorkspace.get()
    }

    private fun poll() {
        try {
            val workspace = fetcher.fetchIfModified() ?: return
            currentWorkspace.set(workspace)
        } catch (e: Exception) {
            log.error { "Failed to poll WorkspaceConfig: $e" }
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
        private val log = Logger<PollingWorkspaceConfigFetcher>()
    }
}
