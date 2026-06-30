package io.hackle.sdk.core.evaluation.service.remoteconfig

import io.hackle.sdk.core.evaluation.evaluator.Evaluator

interface RemoteConfigEvaluator<T : Any, REQUEST : RemoteConfigEvaluateRequest<T>> :
    Evaluator<REQUEST, RemoteConfigEvaluateResponse<T>>
