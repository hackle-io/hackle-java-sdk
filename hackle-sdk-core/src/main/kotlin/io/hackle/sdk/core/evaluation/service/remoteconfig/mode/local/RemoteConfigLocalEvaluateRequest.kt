package io.hackle.sdk.core.evaluation.service.remoteconfig.mode.local

import io.hackle.sdk.core.evaluation.mode.local.LocalEvaluateRequest
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateRequest
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.config.WorkspaceConfig
import io.hackle.sdk.core.workspace.config.entity.RemoteConfigParameterConfig

class RemoteConfigLocalEvaluateRequest private constructor(
    override val workspace: WorkspaceConfig,
    override val entity: RemoteConfigParameterConfig,
    override val user: HackleUser,
    override val record: Boolean,
    override val requiredType: ValueType,
) : LocalEvaluateRequest(), RemoteConfigEvaluateRequest {

    companion object {
        fun of(
            workspace: WorkspaceConfig,
            parameter: RemoteConfigParameterConfig,
            user: HackleUser,
            requiredType: ValueType,
            record: Boolean = true,
        ): RemoteConfigLocalEvaluateRequest {
            return RemoteConfigLocalEvaluateRequest(workspace, parameter, user, record, requiredType)
        }
    }
}
