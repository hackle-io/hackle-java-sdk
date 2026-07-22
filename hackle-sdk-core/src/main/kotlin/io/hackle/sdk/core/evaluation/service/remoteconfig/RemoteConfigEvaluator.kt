package io.hackle.sdk.core.evaluation.service.remoteconfig

import io.hackle.sdk.core.evaluation.evaluator.Evaluator

interface RemoteConfigEvaluator<REQUEST : RemoteConfigEvaluateRequest> :
    Evaluator<REQUEST, RemoteConfigEvaluateResponse>
