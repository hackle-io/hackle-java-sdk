package io.hackle.sdk.core.evaluation.service.remoteconfig

import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.RemoteConfigParameter

class RemoteConfigEvaluation<out T>(
    override val entity: RemoteConfigParameter,
    override val result: RemoteConfigEvaluateResult<T>,
    val properties: Map<String, Any>,
) : Evaluation
