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
    override val record: Boolean,
    override val requiredType: ValueType,
) : RemoteEvaluateRequest(), RemoteConfigEvaluateRequest {
    companion object {
        fun of(
            workspace: WorkspaceEvaluation,
            parameter: RemoteConfigParameterRemoteEvaluateResult,
            user: HackleUser,
            requiredType: ValueType,
            record: Boolean = true,
        ): RemoteConfigRemoteEvaluateRequest {
            return RemoteConfigRemoteEvaluateRequest(workspace, parameter, user, record, requiredType)
        }
    }
}
