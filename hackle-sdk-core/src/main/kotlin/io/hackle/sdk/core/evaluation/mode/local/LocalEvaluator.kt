package io.hackle.sdk.core.evaluation.mode.local

import io.hackle.sdk.core.evaluation.EvaluateResponse
import io.hackle.sdk.core.evaluation.evaluator.ContextualEvaluator

abstract class LocalEvaluator<REQUEST : LocalEvaluateRequest, RESPONSE : EvaluateResponse> :
    ContextualEvaluator<REQUEST, RESPONSE>()
