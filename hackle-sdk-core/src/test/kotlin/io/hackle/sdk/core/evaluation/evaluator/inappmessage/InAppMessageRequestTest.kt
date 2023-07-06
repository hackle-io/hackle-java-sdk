package io.hackle.sdk.core.evaluation.evaluator.inappmessage

import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import io.hackle.sdk.core.workspace.Workspace
import io.mockk.mockk
import java.util.*

internal fun inAppMessageRequest(
    workspace: Workspace = mockk(),
    user: HackleUser = HackleUser.builder().identifier(IdentifierType.ID, UUID.randomUUID().toString()).build(),
    inAppMessage: InAppMessage = mockk(),
    currentTimeMillis: Long = mockk()
): InAppMessageRequest {
    return InAppMessageRequest(workspace, user, inAppMessage, currentTimeMillis)
}
