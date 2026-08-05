package io.hackle.sdk.core.workspace.config

import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.core.workspace.WorkspaceFetcher

interface WorkspaceConfigFetcher : WorkspaceFetcher {
    override fun metadata(): Workspace.Metadata?
    override fun workspace(user: HackleUser): WorkspaceConfig?
}
