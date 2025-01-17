package io.hackle.sdk.core.internal.threads

import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

internal class PoolingExecutorsTest {

    @Test
    fun `newThreadPool`() {
        val factory = mockk<ThreadFactory>()
        val actual = PoolingExecutors.newThreadPool(2, 4, factory)
        expectThat(actual)
            .isA<ThreadPoolExecutor>()
            .and {
                get { corePoolSize } isEqualTo 2
                get { maximumPoolSize } isEqualTo 2
                get { getKeepAliveTime(TimeUnit.MILLISECONDS) } isEqualTo 0
                get { queue }.isA<ArrayBlockingQueue<*>>()
                get { threadFactory } isSameInstanceAs factory
            }
    }
}