package io.hackle.sdk.core.evaluation.evaluator.inappmessage.storage

import io.hackle.sdk.core.model.InAppMessage

interface HackleInAppMessageStorage {

    fun exist(inAppMessage: InAppMessage, nowTimeMillis: Long): Boolean

    fun put(inAppMessage: InAppMessage, expiredAtMillis: Long)

}

class DefaultHackleInAppMessageStorage : HackleInAppMessageStorage {

    override fun exist(inAppMessage: InAppMessage, nowTimeMillis: Long): Boolean {
        return false
    }

    override fun put(inAppMessage: InAppMessage, expiredAtMillis: Long) {
        // Do Nothing
    }
}
