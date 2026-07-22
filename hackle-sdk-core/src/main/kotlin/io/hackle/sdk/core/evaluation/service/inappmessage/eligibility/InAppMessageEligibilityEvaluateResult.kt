package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.EvaluateResult

interface InAppMessageEligibilityEvaluateResult : EvaluateResult {
    val isEligible: Boolean

    companion object {
        fun eligible(reason: DecisionReason): InAppMessageEligibilityEvaluateResult {
            return DefaultInAppMessageEligibilityEvaluateResult(reason = reason, isEligible = true)
        }

        fun ineligible(reason: DecisionReason): InAppMessageEligibilityEvaluateResult {
            return DefaultInAppMessageEligibilityEvaluateResult(reason = reason, isEligible = false)
        }
    }
}

private class DefaultInAppMessageEligibilityEvaluateResult(
    override val reason: DecisionReason,
    override val isEligible: Boolean,
) : InAppMessageEligibilityEvaluateResult
