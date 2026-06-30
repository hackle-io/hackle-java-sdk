package io.hackle.sdk.core.evaluation.service.remoteconfig.mode.remote

import io.hackle.sdk.core.evaluation.mode.remote.RemoteEvaluateRequest
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateRequest
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.evaluation.WorkspaceEvaluation
import io.hackle.sdk.core.workspace.evaluation.entity.RemoteConfigParameterRemoteEvaluateResult

class RemoteConfigRemoteEvaluateRequest<out T : Any> private constructor(
    override val workspace: WorkspaceEvaluation,
    override val entity: RemoteConfigParameterRemoteEvaluateResult,
    override val user: HackleUser,
    override val record: Boolean,
    override val requiredType: ValueType,
    override val defaultValue: T,
) : RemoteEvaluateRequest(), RemoteConfigEvaluateRequest<T> {
    companion object {
        fun <T : Any> of(
            workspace: WorkspaceEvaluation,
            parameter: RemoteConfigParameterRemoteEvaluateResult,
            user: HackleUser,
            requiredType: ValueType,
            defaultValue: T,
            record: Boolean = true,
        ): RemoteConfigRemoteEvaluateRequest<T> {
            return RemoteConfigRemoteEvaluateRequest(workspace, parameter, user, record, requiredType, defaultValue)
        }
    }
}
