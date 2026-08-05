package io.hackle.sdk.core.workspace.evaluation

import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.core.workspace.WorkspaceFetcher

interface WorkspaceEvaluationFetcher : WorkspaceFetcher {
    override fun metadata(): Workspace.Metadata?
    override fun workspace(user: HackleUser): WorkspaceEvaluation?
}
