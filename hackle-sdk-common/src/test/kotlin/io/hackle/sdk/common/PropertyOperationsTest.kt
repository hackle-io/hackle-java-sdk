package io.hackle.sdk.common

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs
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
    fun `set - properties 로 SET operation 을 생성한다`() {
        val operations = PropertyOperations.set(mapOf("age" to 42, "grade" to "GOLD"))

        expectThat(operations) {
            get { size } isEqualTo 1
            get { this[PropertyOperation.SET] } isEqualTo mapOf("age" to 42, "grade" to "GOLD")
        }
    }

    @Test
    fun `set - 빈 properties 면 empty 를 리턴한다`() {
        expectThat(PropertyOperations.set(emptyMap())) isSameInstanceAs PropertyOperations.empty()
    }

    @Test
    fun `asMap - operation 별 properties 를 리턴한다`() {
        val operations = PropertyOperations.builder()
            .set("age", 42)
            .unset("grade")
            .build()

        expectThat(operations.asMap()) isEqualTo mapOf(
            PropertyOperation.SET to mapOf<String, Any>("age" to 42),
            PropertyOperation.UNSET to mapOf<String, Any>("grade" to "-")
        )
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
}