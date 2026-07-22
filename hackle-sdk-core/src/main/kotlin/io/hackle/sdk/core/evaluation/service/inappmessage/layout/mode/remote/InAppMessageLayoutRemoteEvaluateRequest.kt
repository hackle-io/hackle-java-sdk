package io.hackle.sdk.core.evaluation.service.inappmessage.layout.mode.remote

import io.hackle.sdk.core.evaluation.mode.remote.RemoteEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.InAppMessageEvaluateScope
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.mode.remote.InAppMessageEligibilityRemoteEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluateRequest
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.evaluation.WorkspaceEvaluation
import io.hackle.sdk.core.workspace.evaluation.entity.InAppMessageLayoutRemoteEvaluateResult

class InAppMessageLayoutRemoteEvaluateRequest private constructor(
    override val workspace: WorkspaceEvaluation,
    override val entity: InAppMessageLayoutRemoteEvaluateResult,
    override val user: HackleUser,
    override val scope: InAppMessageEvaluateScope,
    override val record: Boolean,
) : RemoteEvaluateRequest(), InAppMessageLayoutEvaluateRequest {
    companion object {

        fun of(
            workspace: WorkspaceEvaluation,
            entity: InAppMessageLayoutRemoteEvaluateResult,
            user: HackleUser,
            scope: InAppMessageEvaluateScope,
            record: Boolean = true,
        ): InAppMessageLayoutRemoteEvaluateRequest {
            return InAppMessageLayoutRemoteEvaluateRequest(
                workspace = workspace,
                entity = entity,
                user = user,
                scope = scope,
                record = record
            )
        }

        fun of(
            request: InAppMessageEligibilityRemoteEvaluateRequest,
            inAppMessage: InAppMessageLayoutRemoteEvaluateResult,
        ): InAppMessageLayoutRemoteEvaluateRequest {
            return InAppMessageLayoutRemoteEvaluateRequest(
                workspace = request.workspace,
                entity = inAppMessage,
                user = request.user,
                scope = request.scope,
                record = request.record
            )
        }
    }
}
