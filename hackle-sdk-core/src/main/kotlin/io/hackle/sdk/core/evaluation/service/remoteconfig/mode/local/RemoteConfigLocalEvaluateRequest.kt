package io.hackle.sdk.core.evaluation.service.remoteconfig.mode.local

import io.hackle.sdk.core.evaluation.mode.local.LocalEvaluateRequest
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateRequest
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.config.WorkspaceConfig
import io.hackle.sdk.core.workspace.config.entity.RemoteConfigParameterConfig

class RemoteConfigLocalEvaluateRequest<out T : Any> private constructor(
    override val workspace: WorkspaceConfig,
    override val entity: RemoteConfigParameterConfig,
    override val user: HackleUser,
    override val record: Boolean,
    override val requiredType: ValueType,
    override val defaultValue: T,
) : LocalEvaluateRequest(), RemoteConfigEvaluateRequest<T> {

    companion object {
        fun <T : Any> of(
            workspace: WorkspaceConfig,
            parameter: RemoteConfigParameterConfig,
            user: HackleUser,
            requiredType: ValueType,
            defaultValue: T,
            record: Boolean = true,
        ): RemoteConfigLocalEvaluateRequest<T> {
            return RemoteConfigLocalEvaluateRequest(workspace, parameter, user, record, requiredType, defaultValue)
        }
    }
}
