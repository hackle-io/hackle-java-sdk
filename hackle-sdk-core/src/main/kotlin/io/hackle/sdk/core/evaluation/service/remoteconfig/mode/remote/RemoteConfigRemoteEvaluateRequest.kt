package io.hackle.sdk.core.evaluation.service.remoteconfig.mode.remote

import io.hackle.sdk.core.evaluation.mode.remote.RemoteEvaluateRequest
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateRequest
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.evaluation.WorkspaceEvaluation
import io.hackle.sdk.core.workspace.evaluation.entity.RemoteConfigParameterRemoteEvaluateResult

class RemoteConfigRemoteEvaluateRequest private constructor(
    override val workspace: WorkspaceEvaluation,
    override val entity: RemoteConfigParameterRemoteEvaluateResult,
    override val user: HackleUser,
    override val requiredType: ValueType,
    override val record: Boolean,
) : RemoteEvaluateRequest(), RemoteConfigEvaluateRequest {
    companion object {
        fun of(
            workspace: WorkspaceEvaluation,
            entity: RemoteConfigParameterRemoteEvaluateResult,
            user: HackleUser,
            requiredType: ValueType,
            record: Boolean = true,
        ): RemoteConfigRemoteEvaluateRequest {
            return RemoteConfigRemoteEvaluateRequest(
                workspace = workspace,
                entity = entity,
                user = user,
                requiredType = requiredType,
                record = record
            )
        }
    }
}
