package io.hackle.sdk.core.evaluation.service.inappmessage.layout.mode.local

import io.hackle.sdk.core.evaluation.mode.local.LocalEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.InAppMessageEvaluateScope
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.mode.local.InAppMessageEligibilityLocalEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluateRequest
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.config.WorkspaceConfig
import io.hackle.sdk.core.workspace.config.entity.InAppMessageConfig

class InAppMessageLayoutLocalEvaluateRequest(
    override val workspace: WorkspaceConfig,
    override val entity: InAppMessageConfig,
    override val user: HackleUser,
    override val record: Boolean,
    override val scope: InAppMessageEvaluateScope,
) : LocalEvaluateRequest(), InAppMessageLayoutEvaluateRequest {
    companion object {

        fun of(
            workspace: WorkspaceConfig,
            entity: InAppMessageConfig,
            user: HackleUser,
            scope: InAppMessageEvaluateScope,
            record: Boolean = true,
        ): InAppMessageLayoutLocalEvaluateRequest {
            return InAppMessageLayoutLocalEvaluateRequest(workspace, entity, user, record, scope)
        }

        fun of(request: InAppMessageEligibilityLocalEvaluateRequest): InAppMessageLayoutLocalEvaluateRequest {
            return InAppMessageLayoutLocalEvaluateRequest(
                workspace = request.workspace,
                entity = request.entity,
                user = request.user,
                record = request.record,
                scope = request.scope
            )
        }
    }
}
