package io.hackle.sdk.core.evaluation.service.remoteconfig

import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.RemoteConfigParameter

class RemoteConfigEvaluation(
    override val entity: RemoteConfigParameter,
    override val result: RemoteConfigEvaluateResult,
) : Evaluation
