package io.hackle.sdk.core.evaluation.evaluator.remoteconfig

import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import io.hackle.sdk.core.workspace.Workspace
import io.mockk.mockk
import java.util.*

internal fun <T : Any> remoteConfigRequest(
    workspace: Workspace = mockk(),
    user: HackleUser = HackleUser.builder().identifier(IdentifierType.ID, UUID.randomUUID().toString()).build(),
    parameter: RemoteConfigParameter = RemoteConfigParameter(
        1,
        "key",
        ValueType.STRING,
        "\$id",
        emptyList(),
        RemoteConfigParameter.Value(1, "defaultValue")
    ),
    requiredType: ValueType = ValueType.STRING,
    defaultValue: T = "default value" as T
): RemoteConfigRequest<T> {
    return RemoteConfigRequest(workspace, user, parameter, requiredType, defaultValue)
}