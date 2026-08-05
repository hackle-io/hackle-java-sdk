package io.hackle.sdk.core.evaluation

import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

interface EvaluateResponse {
    val user: HackleUser
    val workspace: Workspace
    val evaluation: Evaluation
    val references: List<Evaluation>
}
