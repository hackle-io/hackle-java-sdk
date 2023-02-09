package io.hackle.sdk.core.internal.log.delegate

import io.hackle.sdk.core.internal.log.Logger
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

internal class DelegatingLoggerTest {

    @Test
    fun `delegate logger`() {
        val delegating = DelegatingLogger("logger")

        val logger = mockk<Logger>(relaxed = true)
        delegating.add { logger }

        repeat(1) { delegating.debug { "test" } }
        repeat(2) { delegating.info { "test" } }
        repeat(3) { delegating.warn { "test" } }
        repeat(4) { delegating.error { "test" } }
        delegating.error(IllegalArgumentException()) { "test" }

        verify(exactly = 1) { logger.debug(any()) }
        verify(exactly = 2) { logger.info(any()) }
        verify(exactly = 3) { logger.warn(any()) }
        verify(exactly = 4) { logger.error(any()) }
        verify(exactly = 1) { logger.error(any(), any()) }
    }

    @Test
    fun `concurrency read write`() {

        val executor = Executors.newFixedThreadPool(32)
        val delegatingLogger = DelegatingLogger("logger")
        val loggerStub = LoggerStub()
        val latch = CountDownLatch(10000)
        repeat(10000) {
            executor.execute {
                if (it % 2 == 0) {
                    delegatingLogger.info { "test" }
                } else {
                    delegatingLogger.add(LoggerStub.Factory(loggerStub))
                }
                latch.countDown()
            }
        }
        latch.await()

        delegatingLogger.debug { "test" }
        expectThat(loggerStub.count.get()).isEqualTo(5000)
    }

    private class LoggerStub : Logger {
        val count = AtomicInteger()
        override fun debug(msg: () -> String) {
            count.incrementAndGet()
        }

        override fun info(msg: () -> String) {}
        override fun warn(msg: () -> String) {}
        override fun error(msg: () -> String) {}
        override fun error(x: Throwable, msg: () -> String) {}

        class Factory(private val logger: Logger) : Logger.Factory {
            override fun getLogger(name: String): Logger {
                return logger
            }
        }
    }
}