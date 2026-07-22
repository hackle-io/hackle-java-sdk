package io.hackle.sdk.core.workspace.config.entity

import io.hackle.sdk.core.model.AbstractRemoteConfigParameter
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.model.ValueType

class RemoteConfigParameterConfig(
    override val id: Long,
    override val key: String,
    override val type: ValueType,
    val identifierType: String,
    val targetRules: List<RemoteConfigParameter.TargetRule>,
    val defaultValue: RemoteConfigParameter.Value,
) : AbstractRemoteConfigParameter(), ConfigEntity
