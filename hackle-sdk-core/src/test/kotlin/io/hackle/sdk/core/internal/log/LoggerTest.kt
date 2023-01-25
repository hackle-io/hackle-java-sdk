package io.hackle.sdk.core.internal.log

import io.hackle.sdk.core.internal.log.delegate.DelegatingLogger
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.util.concurrent.atomic.AtomicInteger

internal class LoggerTest {

    @Test
    fun `default logger`() {
        expectThat(Logger<LoggerTest>()).isA<DelegatingLogger>()
        expectThat(Logger("a")).isA<DelegatingLogger>()
        expectThat(Logger.factory.getLogger("b")).isA<DelegatingLogger>()
    }

    @Test
    fun `add logger`() {
        Logger.add(LoggerStub.Factory)

        val logger = Logger("test")
        logger.debug { "test" }
        logger.info { "test" }
        logger.info { "test" }
        logger.warn { "test" }
        logger.warn { "test" }
        logger.warn { "test" }

        expectThat(LoggerStub.count[LogLevel.DEBUG]?.get()).isNotNull().isEqualTo(1)
        expectThat(LoggerStub.count[LogLevel.INFO]?.get()).isNotNull().isEqualTo(2)
        expectThat(LoggerStub.count[LogLevel.WARN]?.get()).isNotNull().isEqualTo(3)
        expectThat(LoggerStub.count[LogLevel.ERROR]?.get()).isNotNull().isEqualTo(0)
    }

    object LoggerStub : Logger {
        val count = LogLevel.values().associateWith { AtomicInteger() }
        private fun count(level: LogLevel) {
            count.getValue(level).incrementAndGet()
        }

        override fun debug(msg: () -> String) = count(LogLevel.DEBUG)
        override fun info(msg: () -> String) = count(LogLevel.INFO)
        override fun warn(msg: () -> String) = count(LogLevel.WARN)
        override fun error(msg: () -> String) = count(LogLevel.ERROR)
        override fun error(x: Throwable, msg: () -> String) = count(LogLevel.ERROR)

        object Factory : Logger.Factory {
            override fun getLogger(name: String): Logger = LoggerStub
        }
    }


}
