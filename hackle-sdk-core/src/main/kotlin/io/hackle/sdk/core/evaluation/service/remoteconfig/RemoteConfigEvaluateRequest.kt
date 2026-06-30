package io.hackle.sdk.core.evaluation.service.remoteconfig

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.model.ValueType

interface RemoteConfigEvaluateRequest<out T : Any> : EvaluateRequest {
    override val entity: RemoteConfigParameter
    val requiredType: ValueType
    val defaultValue: T
}
