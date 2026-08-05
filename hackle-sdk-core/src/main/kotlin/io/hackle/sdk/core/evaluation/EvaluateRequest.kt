package io.hackle.sdk.core.evaluation

import io.hackle.sdk.core.model.Entity
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

interface EvaluateRequest {
    val phase: EvaluationPhase
    val user: HackleUser
    val workspace: Workspace
    val entity: Entity
    val record: Boolean
}
