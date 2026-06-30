package io.hackle.sdk.core.evaluation.mode.local

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.workspace.config.WorkspaceConfig
import io.hackle.sdk.core.workspace.config.entity.ConfigEntity

abstract class LocalEvaluateRequest : EvaluateRequest {
    abstract override val workspace: WorkspaceConfig
    abstract override val entity: ConfigEntity
}
