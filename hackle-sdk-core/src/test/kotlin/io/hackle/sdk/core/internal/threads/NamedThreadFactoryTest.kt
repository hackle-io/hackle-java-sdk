package io.hackle.sdk.core.internal.threads

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

internal class NamedThreadFactoryTest {

    @Test
    fun `newThread`() {
        val namedThreadFactory = NamedThreadFactory("test-", true)

        val thread1 = namedThreadFactory.newThread {}

        expectThat(thread1) {
            get { name } isEqualTo "test-1"
            get { isDaemon }.isTrue()
        }

        val thread2 = namedThreadFactory.newThread {}
        expectThat(thread2) {
            get { name } isEqualTo "test-2"
            get { isDaemon }.isTrue()
        }
    }
}