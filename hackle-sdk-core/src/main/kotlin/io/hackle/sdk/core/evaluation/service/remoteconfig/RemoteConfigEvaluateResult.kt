package io.hackle.sdk.core.evaluation.service.remoteconfig

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.EvaluateResult

interface RemoteConfigEvaluateResult<out T> : EvaluateResult {
    val value: T
    val valueId: Long?

    private class DefaultRemoteConfigEvaluateResult<out T>(
        override val reason: DecisionReason,
        override val value: T,
        override val valueId: Long?,
    ) : RemoteConfigEvaluateResult<T>

    companion object {
        fun <T : Any> of(reason: DecisionReason, value: T, valueId: Long?): RemoteConfigEvaluateResult<T> {
            return DefaultRemoteConfigEvaluateResult(reason = reason, value = value, valueId = valueId)
        }
    }
}
