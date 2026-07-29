package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility

import io.hackle.sdk.core.evaluation.evaluator.Evaluator

interface InAppMessageEligibilityEvaluator<REQUEST : InAppMessageEligibilityEvaluateRequest> :
    Evaluator<REQUEST, InAppMessageEligibilityEvaluateResponse>
