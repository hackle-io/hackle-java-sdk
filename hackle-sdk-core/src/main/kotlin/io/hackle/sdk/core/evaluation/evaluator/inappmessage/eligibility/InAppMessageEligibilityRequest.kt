package io.hackle.sdk.core.evaluation.evaluator.inappmessage.eligibility

import io.hackle.sdk.core.evaluation.evaluator.inappmessage.InAppMessageEvaluatorRequest
import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

class InAppMessageEligibilityRequest(
    override val workspace: Workspace,
    override val user: HackleUser,
    override val inAppMessage: InAppMessage,
    val timestamp: Long,
) : InAppMessageEvaluatorRequest()
