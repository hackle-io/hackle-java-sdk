package io.hackle.sdk.core.evaluation.service.remoteconfig

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.EvaluateResult
import io.hackle.sdk.core.model.RemoteConfigParameter

interface RemoteConfigEvaluateResult : EvaluateResult {
    val value: RemoteConfigParameter.Value?

    companion object {
        fun of(reason: DecisionReason, value: RemoteConfigParameter.Value?): RemoteConfigEvaluateResult {
            return DefaultRemoteConfigEvaluateResult(reason = reason, value = value)
        }
    }
}

private class DefaultRemoteConfigEvaluateResult(
    override val reason: DecisionReason,
    override val value: RemoteConfigParameter.Value?,
) : RemoteConfigEvaluateResult
