package io.hackle.sdk.core.internal.threads

import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

/**
 * @author Yong
 */
class NamedThreadFactory(
    private val namePrefix: String,
    private val isDaemon: Boolean
) : ThreadFactory {

    private val count = AtomicLong(1)
    private val delegate = Executors.defaultThreadFactory()

    override fun newThread(r: Runnable): Thread {
        return delegate.newThread(r).also {
            it.name = namePrefix + count.getAndIncrement()
            it.isDaemon = isDaemon
        }
    }
}
