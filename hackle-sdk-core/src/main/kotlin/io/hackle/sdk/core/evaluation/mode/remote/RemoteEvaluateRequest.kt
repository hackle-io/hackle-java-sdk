package io.hackle.sdk.core.evaluation.mode.remote

import io.hackle.sdk.core.evaluation.AbstractEvaluateRequest
import io.hackle.sdk.core.evaluation.EvaluationPhase
import io.hackle.sdk.core.workspace.evaluation.WorkspaceEvaluation
import io.hackle.sdk.core.workspace.evaluation.entity.RemoteEvaluateResult

abstract class RemoteEvaluateRequest : AbstractEvaluateRequest() {
    abstract override val workspace: WorkspaceEvaluation
    abstract override val entity: RemoteEvaluateResult

    final override val phase: EvaluationPhase get() = EvaluationPhase.RUNTIME
}
