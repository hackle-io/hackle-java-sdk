package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility

import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.InAppMessage

class InAppMessageEligibilityEvaluation(
    override val entity: InAppMessage,
    override val result: InAppMessageEligibilityEvaluateResult,
) : Evaluation
