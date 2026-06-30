package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.mode.local

import io.hackle.sdk.core.evaluation.mode.local.LocalEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.InAppMessageEvaluateScope
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateRequest
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.config.WorkspaceConfig
import io.hackle.sdk.core.workspace.config.entity.InAppMessageConfig

class InAppMessageEligibilityLocalEvaluateRequest private constructor(
    override val workspace: WorkspaceConfig,
    override val entity: InAppMessageConfig,
    override val user: HackleUser,
    override val scope: InAppMessageEvaluateScope,
    override val timestamp: Long,
) : LocalEvaluateRequest(), InAppMessageEligibilityEvaluateRequest {
    override val inAppMessage: InAppMessageConfig get() = entity
    override val record: Boolean get() = true

    companion object {
        fun of(
            workspace: WorkspaceConfig,
            entity: InAppMessageConfig,
            user: HackleUser,
            scope: InAppMessageEvaluateScope,
            timestamp: Long,
        ): InAppMessageEligibilityLocalEvaluateRequest {
            return InAppMessageEligibilityLocalEvaluateRequest(workspace, entity, user, scope, timestamp)
        }
    }
}
