package io.hackle.sdk.core.model

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isNotNull
import strikt.assertions.isNull

internal class ContainerTest {

    @Test
    fun `getGroupOrNull`() {

        assertNull(Container(42, 320, listOf()).getGroupOrNull(1))

        val container = Container(42, 320, listOf(ContainerGroup(99, listOf())))
        expectThat(container.getGroupOrNull(100)).isNull()
        expectThat(container.getGroupOrNull(99)).isNotNull()
    }
}