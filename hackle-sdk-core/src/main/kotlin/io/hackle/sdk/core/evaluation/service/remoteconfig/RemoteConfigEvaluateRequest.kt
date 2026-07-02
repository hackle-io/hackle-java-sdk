package io.hackle.sdk.core.evaluation.service.remoteconfig

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.model.ValueType

interface RemoteConfigEvaluateRequest : EvaluateRequest {
    override val entity: RemoteConfigParameter
    val requiredType: ValueType
}
