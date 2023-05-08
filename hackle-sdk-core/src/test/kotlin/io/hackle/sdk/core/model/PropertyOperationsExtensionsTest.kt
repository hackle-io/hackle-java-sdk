package io.hackle.sdk.core.model

import io.hackle.sdk.common.PropertyOperations
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class PropertyOperationsExtensionsTest {

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