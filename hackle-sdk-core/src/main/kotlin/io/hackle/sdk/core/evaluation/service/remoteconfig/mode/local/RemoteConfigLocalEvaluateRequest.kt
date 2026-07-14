package io.hackle.sdk.core.evaluation.service.remoteconfig.mode.local

import io.hackle.sdk.core.evaluation.EvaluationPhase
import io.hackle.sdk.core.evaluation.mode.local.LocalEvaluateRequest
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateRequest
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.config.WorkspaceConfig
import io.hackle.sdk.core.workspace.config.entity.RemoteConfigParameterConfig

class RemoteConfigLocalEvaluateRequest private constructor(
    override val phase: EvaluationPhase,
    override val workspace: WorkspaceConfig,
    override val entity: RemoteConfigParameterConfig,
    override val user: HackleUser,
    override val requiredType: ValueType,
    override val record: Boolean,
) : LocalEvaluateRequest(), RemoteConfigEvaluateRequest {

    companion object {
        fun of(
            workspace: WorkspaceConfig,
            entity: RemoteConfigParameterConfig,
            user: HackleUser,
            requiredType: ValueType,
            phase: EvaluationPhase = EvaluationPhase.RUNTIME,
            record: Boolean = true,
        ): RemoteConfigLocalEvaluateRequest {
            return RemoteConfigLocalEvaluateRequest(
                phase = phase,
                workspace = workspace,
                entity = entity,
                user = user,
                requiredType = requiredType,
                record = record,
            )
        }
    }
}
