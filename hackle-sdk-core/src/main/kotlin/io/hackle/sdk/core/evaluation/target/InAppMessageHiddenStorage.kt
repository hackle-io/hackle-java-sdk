package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.model.InAppMessage

interface InAppMessageHiddenStorage {
    fun exist(inAppMessage: InAppMessage, now: Long): Boolean
    fun put(inAppMessage: InAppMessage, expireAt: Long)
}

internal object NoopInAppMessageHiddenStorage : InAppMessageHiddenStorage {
    override fun exist(inAppMessage: InAppMessage, now: Long): Boolean {
        return false
    }

    override fun put(inAppMessage: InAppMessage, expireAt: Long) {
        // Noop
    }
}
