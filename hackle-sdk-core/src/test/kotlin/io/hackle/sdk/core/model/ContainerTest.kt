package io.hackle.sdk.core.model

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

internal class ContainerTest {

    @Test
    fun `Container`() {

        assertNull(Container(42, 320, listOf()).getGroupOrNull(1))

        val container = Container(42, 320, listOf(ContainerGroup(99, listOf())))
        expectThat(container.getGroupOrNull(100)).isNull()
        expectThat(container.getGroupOrNull(99)).isNotNull()

        expectThat(container) {
            get { id } isEqualTo 42
            get { bucketId } isEqualTo 320
            get { groups }.hasSize(1)
        }
    }
}