package io.hackle.sdk.core.internal.threads

import java.util.concurrent.*

/**
 * @author Yong
 */
object PoolingExecutors {

    fun newThreadPool(poolSize: Int, workQueueCapacity: Int, threadFactory: ThreadFactory): ExecutorService {
        return ThreadPoolExecutor(
            poolSize, poolSize,
            0, TimeUnit.MILLISECONDS,
            ArrayBlockingQueue(workQueueCapacity),
            threadFactory
        )
    }
}
