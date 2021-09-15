package io.hackle.sdk.core.internal.log

import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isSameInstanceAs

internal class NoopLoggerTest {

    @Test
    fun `noop`() {
        val msg = mockk<() -> String>()
        NoopLogger.info(msg)
        NoopLogger.warn(msg)
        NoopLogger.error(msg)
        NoopLogger.error(IllegalArgumentException(), msg)
        verify {
            msg wasNot Called
        }
    }

    @Test
    fun `factory`() {
        expectThat(NoopLogger.Factory.getLogger("a"))
            .isSameInstanceAs(NoopLogger.Factory.getLogger("b"))
            .isSameInstanceAs(NoopLogger.Factory.getLogger(NoopLoggerTest::class.java))
    }
}