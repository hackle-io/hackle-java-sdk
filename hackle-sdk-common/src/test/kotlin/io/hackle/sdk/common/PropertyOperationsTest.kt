package io.hackle.sdk.common

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

internal class PropertyOperationsTest {

    @Test
    fun `empty`() {
        expectThat(PropertyOperations.empty()) {
            get { size } isEqualTo 0
        }
    }

    @Test
    fun `clearAll`() {
        expectThat(PropertyOperations.clearAll()) {
            get { size } isEqualTo 1
            get { contains(PropertyOperation.CLEAR_ALL) }.isTrue()
        }
    }

    @Test
    fun `build`() {

        val operations = PropertyOperations.builder()
            .set("set1", 42)
            .set("set2", listOf("a", "b"))
            .set("set2", "set2")
            .setOnce("setOnce", 43)
            .unset("unset")
            .increment("increment", 44)
            .append("append", 45)
            .appendOnce("appendOnce", 46)
            .prepend("prepend", 47)
            .prependOnce("prependOnce", 48)
            .remove("remove", 49)
            .clearAll()
            .build()

        expectThat(operations) {
            get { size } isEqualTo 10
            get { this[PropertyOperation.SET] } isEqualTo mapOf("set1" to 42, "set2" to listOf("a", "b"))
            get { this[PropertyOperation.SET_ONCE] } isEqualTo mapOf("setOnce" to 43)
            get { this[PropertyOperation.UNSET] } isEqualTo mapOf("unset" to "-")
            get { this[PropertyOperation.INCREMENT] } isEqualTo mapOf("increment" to 44)
            get { this[PropertyOperation.APPEND] } isEqualTo mapOf("append" to 45)
            get { this[PropertyOperation.APPEND_ONCE] } isEqualTo mapOf("appendOnce" to 46)
            get { this[PropertyOperation.PREPEND] } isEqualTo mapOf("prepend" to 47)
            get { this[PropertyOperation.PREPEND_ONCE] } isEqualTo mapOf("prependOnce" to 48)
            get { this[PropertyOperation.REMOVE] } isEqualTo mapOf("remove" to 49)
            get { this[PropertyOperation.CLEAR_ALL] } isEqualTo mapOf("clearAll" to "-")
        }
    }

    @Test
    fun `toEvent`() {
        val operations = PropertyOperations.builder()
            .set("set1", 42)
            .set("set2", listOf("a", "b"))
            .set("set2", "set2")
            .setOnce("setOnce", 43)
            .unset("unset")
            .increment("increment", 44)
            .append("append", 45)
            .appendOnce("appendOnce", 46)
            .prepend("prepend", 47)
            .prependOnce("prependOnce", 48)
            .remove("remove", 49)
            .clearAll()
            .build()

        val event = operations.toEvent()

        expectThat(event) {
            get { key } isEqualTo "\$properties"
            get { properties } isEqualTo hashMapOf(
                "\$set" to mapOf("set1" to 42, "set2" to listOf("a", "b")),
                "\$setOnce" to mapOf("setOnce" to 43),
                "\$unset" to mapOf("unset" to "-"),
                "\$increment" to mapOf("increment" to 44),
                "\$append" to mapOf("append" to 45),
                "\$appendOnce" to mapOf("appendOnce" to 46),
                "\$prepend" to mapOf("prepend" to 47),
                "\$prependOnce" to mapOf("prependOnce" to 48),
                "\$remove" to mapOf("remove" to 49),
                "\$clearAll" to mapOf("clearAll" to "-"),
            )
        }
    }
}