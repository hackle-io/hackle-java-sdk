package io.hackle.sdk.core.internal.log.delegate

import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.internal.log.NoopLogger
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

internal class DelegatingLoggerFactoryTest {

    @Test
    fun `Logger 생성 & LoggerFactory 추가 동시성`() {
        val executor = Executors.newFixedThreadPool(32)
        repeat(10) {
            val factory = DelegatingLoggerFactory()
            val logger = LoggerStub()
            val latch = CountDownLatch(1000)
            repeat(1000) {
                executor.execute {
                    if (it % 2 == 0) {
                        factory.add(LoggerStub.Factory(logger))
                    } else {
                        factory.getLogger(it.toString())
                    }
                    latch.countDown()
                }
            }
            latch.await()

            expectThat(factory.loggers.size).isEqualTo(500)
            for (i in 1..1000 step 2) {
                val name = i.toString()
                factory.getLogger(name).debug { "test" }
            }

            expectThat(logger.count.get()).isEqualTo(500 * 500)
        }
    }

    @Test
    fun `DelegatingLoggerFactory 는 추가하지 않는다`() {
        val sut = DelegatingLoggerFactory()
        val delegating = spyk(DelegatingLoggerFactory())
        val noop = spyk(NoopLogger.Factory())
        sut.add(delegating)
        sut.add(noop)

        sut.getLogger("logger")
        verify(exactly = 0) { delegating.getLogger(any()) }
        verify(exactly = 1) { noop.getLogger(any()) }
    }

    @Test
    fun `이미 추가되어있는 Factory 는 추가 하지 않는다`() {
        val sut = DelegatingLoggerFactory()
        val logger = sut.getLogger("logger")

        val loggerStub = LoggerStub()
        val loggerStubFactory = LoggerStub.Factory(loggerStub)

        sut.add(loggerStubFactory)
        sut.add(loggerStubFactory)
        sut.add(loggerStubFactory)

        logger.debug { "test" }

        expectThat(loggerStub.count.get()).isEqualTo(1)
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

        class Factory(private val logger: LoggerStub) : Logger.Factory {
            override fun getLogger(name: String): Logger {
                return logger
            }
        }
    }
}