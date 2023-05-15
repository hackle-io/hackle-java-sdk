package io.hackle.sdk.core.evaluation.evaluator.inappmessage.storage

interface HackleInAppMessageStorage {

    fun getAll(): Map<Long, Long>

    fun getInvisibleUntil(inAppMessageKey: Long): Long

    fun setInvisibleUntil(inAppMessageKey: Long, until: Long)

    fun remove(inAppMessageKey: Long)

    fun clear()

}

class DefaultHackleInAppMessageStorage : HackleInAppMessageStorage {
    override fun getAll(): Map<Long, Long> {
        return emptyMap()
    }

    override fun getInvisibleUntil(inAppMessageKey: Long): Long {
       return -1L
    }

    override fun setInvisibleUntil(inAppMessageKey: Long, until: Long) {}

    override fun remove(inAppMessageKey: Long) {}

    override fun clear() {}
}
