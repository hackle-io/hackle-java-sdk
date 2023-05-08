package io.hackle.sdk.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class PropertyOperationTest {

    @Test
    fun `key`() {
        assertEquals("\$set", PropertyOperation.SET.key)
        assertEquals("\$setOnce", PropertyOperation.SET_ONCE.key)
        assertEquals("\$unset", PropertyOperation.UNSET.key)
        assertEquals("\$increment", PropertyOperation.INCREMENT.key)
        assertEquals("\$append", PropertyOperation.APPEND.key)
        assertEquals("\$appendOnce", PropertyOperation.APPEND_ONCE.key)
        assertEquals("\$prepend", PropertyOperation.PREPEND.key)
        assertEquals("\$prependOnce", PropertyOperation.PREPEND_ONCE.key)
        assertEquals("\$remove", PropertyOperation.REMOVE.key)
        assertEquals("\$clearAll", PropertyOperation.CLEAR_ALL.key)
    }

    @Test
    fun `from`() {
        fun check(key: String, operation: PropertyOperation) {
            expectThat(PropertyOperation.from(key)) isEqualTo operation
        }
        check("\$set", PropertyOperation.SET)
        check("\$setOnce", PropertyOperation.SET_ONCE)
        check("\$unset", PropertyOperation.UNSET)
        check("\$increment", PropertyOperation.INCREMENT)
        check("\$append", PropertyOperation.APPEND)
        check("\$appendOnce", PropertyOperation.APPEND_ONCE)
        check("\$prepend", PropertyOperation.PREPEND)
        check("\$prependOnce", PropertyOperation.PREPEND_ONCE)
        check("\$remove", PropertyOperation.REMOVE)
        check("\$clearAll", PropertyOperation.CLEAR_ALL)
    }
}