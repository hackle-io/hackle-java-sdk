package io.hackle.sdk.core.internal.log

import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA

internal class LoggerTest {

    @Test
    fun `default logger`() {
        val logger = Logger<LoggerTest>()
        expectThat(logger).isA<NoopLogger>()
        expectThat(Logger.factory.getLogger("abc")).isA<NoopLogger>()
        expectThat(Logger.factory.getLogger(LoggerTest::class.java)).isA<NoopLogger>()
    }

    @Test
    fun `Factory getLogger from class`() {
        val sut = spyk(Logger.Factory { NoopLogger })
        sut.getLogger(LoggerTest::class.java)
        verify(exactly = 1) {
            sut.getLogger(any<String>())
        }
    }
}
