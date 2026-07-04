package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.mode.local

import io.hackle.sdk.core.evaluation.mode.local.LocalEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.InAppMessageEvaluateScope
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateRequest
import io.hackle.sdk.core.model.PlatformType
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.config.WorkspaceConfig
import io.hackle.sdk.core.workspace.config.entity.InAppMessageConfig

class InAppMessageEligibilityLocalEvaluateRequest private constructor(
    override val workspace: WorkspaceConfig,
    override val entity: InAppMessageConfig,
    override val user: HackleUser,
    override val record: Boolean,
    override val scope: InAppMessageEvaluateScope,
    override val platformType: PlatformType,
    override val timestamp: Long,
) : LocalEvaluateRequest(), InAppMessageEligibilityEvaluateRequest {
    override val inAppMessage: InAppMessageConfig get() = entity

    companion object {
        fun of(
            workspace: WorkspaceConfig,
            entity: InAppMessageConfig,
            user: HackleUser,
            scope: InAppMessageEvaluateScope,
            platformType: PlatformType,
            timestamp: Long,
            record: Boolean = true,
        ): InAppMessageEligibilityLocalEvaluateRequest {
            return InAppMessageEligibilityLocalEvaluateRequest(
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
