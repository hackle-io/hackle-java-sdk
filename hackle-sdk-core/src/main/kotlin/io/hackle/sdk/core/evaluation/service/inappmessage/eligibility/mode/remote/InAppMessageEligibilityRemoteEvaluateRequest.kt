package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.mode.remote

import io.hackle.sdk.core.evaluation.mode.remote.RemoteEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.InAppMessageEvaluateScope
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateRequest
import io.hackle.sdk.core.model.PlatformType
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.evaluation.WorkspaceEvaluation
import io.hackle.sdk.core.workspace.evaluation.entity.InAppMessageEligibilityRemoteEvaluateResult

class InAppMessageEligibilityRemoteEvaluateRequest private constructor(
    override val workspace: WorkspaceEvaluation,
    override val entity: InAppMessageEligibilityRemoteEvaluateResult,
    override val user: HackleUser,
    override val record: Boolean,
    override val scope: InAppMessageEvaluateScope,
    override val platformType: PlatformType,
    override val timestamp: Long,
) : RemoteEvaluateRequest(), InAppMessageEligibilityEvaluateRequest {
    override val inAppMessage: InAppMessageEligibilityRemoteEvaluateResult get() = entity

    companion object {
        fun of(
            workspace: WorkspaceEvaluation,
            entity: InAppMessageEligibilityRemoteEvaluateResult,
            user: HackleUser,
            scope: InAppMessageEvaluateScope,
            platformType: PlatformType,
            timestamp: Long,
            record: Boolean = true,
        ): InAppMessageEligibilityRemoteEvaluateRequest {
            return InAppMessageEligibilityRemoteEvaluateRequest(
                workspace = workspace,
                entity = entity,
                user = user,
                record = record,
                scope = scope,
                platformType = platformType,
                timestamp = timestamp
            )
        }
    }
}
