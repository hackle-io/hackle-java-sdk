package io.hackle.sdk.core.evaluation.service.inappmessage.layout

import io.hackle.sdk.core.evaluation.evaluator.Evaluator

interface InAppMessageLayoutEvaluator<REQUEST : InAppMessageLayoutEvaluateRequest> :
    Evaluator<REQUEST, InAppMessageLayoutEvaluateResponse>
