package io.hackle.sdk.core.evaluation.service.remoteconfig

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.common.decision.DecisionReason.TYPE_MISMATCH
import io.hackle.sdk.core.evaluation.EvaluateResult
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.model.isInstance

interface RemoteConfigEvaluateResult : EvaluateResult {
    val value: RemoteConfigParameter.Value?

    companion object {
        fun of(reason: DecisionReason, value: RemoteConfigParameter.Value?): RemoteConfigEvaluateResult {
            return DefaultRemoteConfigEvaluateResult(reason = reason, value = value)
        }

        fun of(
            request: RemoteConfigEvaluateRequest,
            value: RemoteConfigParameter.Value?,
            reason: DecisionReason,
        ): RemoteConfigEvaluateResult {
            if (value == null) {
                return of(reason, null)
            }
            return if (request.requiredType.isInstance(value)) {
                of(reason, value)
            } else {
                of(TYPE_MISMATCH, value)
            }
        }
    }
}

private class DefaultRemoteConfigEvaluateResult(
    override val reason: DecisionReason,
    override val value: RemoteConfigParameter.Value?,
) : RemoteConfigEvaluateResult
