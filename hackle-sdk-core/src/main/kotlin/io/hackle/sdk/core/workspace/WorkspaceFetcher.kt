package io.hackle.sdk.core.workspace

import io.hackle.sdk.core.user.HackleUser

interface WorkspaceFetcher {
    fun metadata(): Workspace.Metadata?
    fun workspace(user: HackleUser): Workspace?
}
