package io.hackle.sdk.common

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsKey
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.util.*

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
    fun `build2`() {
        val user = User.builder()
            .id("id")
            .userId("userId")
            .deviceId("deviceId")
            .identifier("id1", "v1")
            .identifiers(mapOf("id2" to "v2"))
            .identifiers(null)
            .property("k1", "v1")
            .properties(mapOf("k2" to 2))
            .properties(null)
            .build()

        expectThat(user) {
            get { id } isEqualTo "id"
            get { userId } isEqualTo "userId"
            get { deviceId } isEqualTo "deviceId"
            get { identifiers } isEqualTo mapOf("id1" to "v1", "id2" to "v2")
            get { properties } isEqualTo mapOf("k1" to "v1", "k2" to 2)
        }

        expectThat(user) isEqualTo user.toBuilder().build()
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

    @Test
    fun `hackle property`() {
        val user = User.builder()
            .id(UUID.randomUUID().toString())
            .platform("Web")
            .osName("Android")
            .osVersion("2.4.3")
            .deviceModel("SM-S906N")
            .deviceType("phone")
            .deviceBrand("samsung")
            .deviceManufacturer("samsung")
            .locale("ko_KR")
            .language("ko")
            .timeZone("Asia.Seoul")
            .screenWidth(1080)
            .screenHeight(1920)
            .packageName("io.hackle")
            .versionCode(23)
            .versionName("2.5.6")
            .isApp(true)
            .build()

        expectThat(user.hackleProperties) isEqualTo mapOf(
            "platform" to "Web",
            "osName" to "Android",
            "osVersion" to "2.4.3",
            "deviceModel" to "SM-S906N",
            "deviceType" to "phone",
            "deviceBrand" to "samsung",
            "deviceManufacturer" to "samsung",
            "locale" to "ko_KR",
            "language" to "ko",
            "timeZone" to "Asia.Seoul",
            "screenWidth" to 1080,
            "screenHeight" to 1920,
            "packageName" to "io.hackle",
            "versionCode" to 23,
            "versionName" to "2.5.6",
            "isApp" to true,
        )
    }
}
