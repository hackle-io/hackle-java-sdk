package io.hackle.sdk.common

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsKey
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

internal class UserTest {

    @Test
    fun `user build`() {
        val user = User.builder("test_id")
            .property("int_key", 42)
            .property("long_key", 42L)
            .property("double_key", 42.0)
            .property("boolean_key", true)
            .property("string_key", "abc 123")
            .property("string_null", null)
            .build()

        expectThat(user) {
            get { id } isEqualTo "test_id"
            get { properties } isEqualTo mapOf(
                "int_key" to 42,
                "long_key" to 42L,
                "double_key" to 42.0,
                "boolean_key" to true,
                "string_key" to "abc 123",
            )
        }
    }

    @Test
    fun `프로퍼티 갯수가 128개가 넘으면 추가하지 않는다`() {
        val user = User.builder("test")
            .apply {
                repeat(200) {
                    property(it.toString(), it)
                }
            }
            .build()

        expectThat(user.properties) {
            hasSize(128)
        }
    }

    @Test
    fun `property key의 길이가 128보다 크면 추가히지 않는다`() {
        val key128 = "a".repeat(128)
        val key129 = "a".repeat(129)


        val user = User.builder("test")
            .property(key128, 128)
            .property(key129, 129)
            .build()

        expectThat(user.properties) {
            hasSize(1)
            containsKey(key128)
            not().containsKey(key129)
        }
    }

    @Test
    fun `프로퍼티가 string 인 경우 1024자를 넘으면 추가하지 않는다`() {
        val v1024 = "a".repeat(1024)
        val v1025 = "a".repeat(1025)

        val user = User.builder("test")
            .property("key", v1024)
            .property("too_long", v1025)
            .build()


        expectThat(user.properties) {
            hasSize(1)
        }
    }
}